package org.sunbird.workflow.service.impl;


import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.sunbird.workflow.config.Configuration;
import org.sunbird.workflow.config.Constants;
import org.sunbird.workflow.exception.ApplicationException;
import org.sunbird.workflow.models.WfRequest;
import org.sunbird.workflow.postgres.entity.WfStatusEntity;
import org.sunbird.workflow.postgres.repo.WfStatusRepo;
import org.sunbird.workflow.service.UserProfileWfService;
import org.sunbird.workflow.service.Workflowservice;

import java.util.*;

@Service
public class UserProfileWfServiceImpl implements UserProfileWfService {

    Logger logger = LogManager.getLogger(UserProfileWfServiceImpl.class);

    @Autowired
    private Workflowservice workflowservice;

    @Autowired
    private RequestServiceImpl requestServiceImpl;

    @Autowired
    private Configuration configuration;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private WfStatusRepo wfStatusRepo;

    /**
     * Update user profile based on wf request
     *
     * @param wfRequest Workflow request
     */
    public void updateUserProfile(WfRequest wfRequest) {
        WfStatusEntity wfStatusEntity = wfStatusRepo.findByApplicationIdAndWfId(wfRequest.getApplicationId(), wfRequest.getWfId());
        if (Constants.PROFILE_SERVICE_NAME.equals(wfRequest.getServiceName()) && Constants.APPROVED_STATE.equals(wfStatusEntity.getCurrentStatus())) {
            updateProfile(wfRequest);
        }
        if (Constants.USER_PROFILE_FLAG_SERVICE.equals(wfRequest.getServiceName()) && Constants.PROCESSED_STATE.equals(wfStatusEntity.getCurrentStatus())) {
            updateProfile(wfRequest);
        }
    }

    private void updateProfile(WfRequest wfRequest){
        StringBuilder builder = new StringBuilder();
        String endPoint = configuration.getHubProfileUpdateEndPoint().replace(Constants.USER_ID_VALUE, wfRequest.getApplicationId());
        builder.append(configuration.getHubServiceHost()).append(endPoint);
        requestServiceImpl.fetchResult(builder, wfRequest.getUpdateFieldValues(), Map.class);
    }


    /**
     *
     * @param wfInfos
     * @param rootOrg
     * @return
     */
    public List<Map<String, Object>> enrichUserData(Map<String, List<WfStatusEntity>> wfInfos, String rootOrg) {
        List<Map<String, Object>> wfDetails = new ArrayList<>();
        HashMap<String, Object> responseMap;
        HashMap<String, Object> userResult = null;
        Set<String> userIds = wfInfos.keySet();
        if (!CollectionUtils.isEmpty(userIds)) {
            List<String> sources = new ArrayList<>(Constants.USER_DEFAULT_FIELDS);
            if (!sources.contains(Constants.UUID)) {
                sources.add(Constants.UUID);
            }
            userResult = getUsersResult(userIds);
        }
        for (Map.Entry<String, List<WfStatusEntity>> wfStatusEntity : wfInfos.entrySet()) {
            responseMap = new HashMap<>();
            responseMap.put("wfInfo", wfStatusEntity.getValue());
            responseMap.put("userInfo", userResult.get(wfStatusEntity.getKey()));
            wfDetails.add(responseMap);
        }
        return wfDetails;
    }

    public HashMap<String, Object> getUsersResult(Set<String> userIds) {
        HashMap<String, Object> userResult = new HashMap<>();
        Map<String, Object> request = getSearchObject(userIds);
        Map<String, Object> record;
        HashMap<String, String> headersValue = new HashMap<>();
        headersValue.put("Content-Type", "application/json");
        try {
            StringBuilder builder = new StringBuilder();
            builder.append(configuration.getLmsServiceHost()).append(configuration.getLmsUserSearchEndPoint());
            Map<String, Object> searchProfileApiResp = (Map<String, Object>) requestServiceImpl.fetchResultUsingPost(builder, request, Map.class, headersValue);
            if (searchProfileApiResp != null && "OK".equalsIgnoreCase((String) searchProfileApiResp.get(Constants.RESPONSE_CODE))) {
                Map<String, Object> map = (Map<String, Object>) searchProfileApiResp.get(Constants.RESULT);
                Map<String, Object> response = (Map<String, Object>) map.get(Constants.RESPONSE);
                List<Map<String, Object>> contents = (List<Map<String, Object>>) response.get(Constants.CONTENT);
                if (!CollectionUtils.isEmpty(contents)) {
                    for (Map<String, Object> content : contents) {
                        HashMap<String, Object> profileDetails = (HashMap<String, Object>) content.get(Constants.PROFILE_DETAILS);
                        HashMap<String, Object> personalDetails = (HashMap<String, Object>) profileDetails.get(Constants.PERSONAL_DETAILS);
                        record = new HashMap<>();
                        record.put(Constants.UUID, profileDetails.get(Constants.USER_ID));
                        record.put(Constants.FIRST_NAME, personalDetails.get(Constants.FIRSTNAME));
                        record.put(Constants.LAST_NAME, personalDetails.get(Constants.LASTNAME));
                        record.put(Constants.EMAIL, personalDetails.get(Constants.PRIMARY_EMAIL));
                        userResult.put(record.get(Constants.UUID).toString(), record);
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e);
            throw new ApplicationException("Hub Service ERROR: ", e);
        }
        return userResult;
    }

    private Map<String, Object> getSearchObject(Set<String> userIds) {
        Map<String, Object> request = new HashMap<>();
        Map<String, Object> filters = new HashMap<>();
        Map<String, Object> idKeyword = new HashMap<>();
        idKeyword.put("or", userIds);
        filters.put("id.keyword", idKeyword);
        request.put("limit", userIds.size());
        request.put("offset", 0);
        request.put("filters", filters);
        return request;
    }

}
