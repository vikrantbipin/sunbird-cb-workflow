package org.sunbird.workflow.service.impl;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
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
            builder.append(configuration.getLmsServiceHost()).append(configuration.getLmsUserSearchEndpoint());
            Map<String, Object> profileResponse = (Map<String, Object>) requestServiceImpl.fetchResultUsingPost(builder, request, Map.class, headersValue);
            logger.info("profile response : {}", profileResponse);
            if (profileResponse != null && "OK".equalsIgnoreCase((String) profileResponse.get("responseCode"))) {
                Map<String, Object> map = (Map<String, Object>) profileResponse.get("result");
                if(map.get("response") != null){
                    Map<String, Object> profiles = (Map<String, Object>) map.get("response");
                    List<Map<String, Object>> userProfiles = (List<Map<String, Object>>) profiles.get("content");
                    if (!CollectionUtils.isEmpty(userProfiles)) {
                        for (Map<String, Object> userProfile : userProfiles) {
                            if(userProfile.get("profileDetails") != null){
                                HashMap<String, Object> profileDetails =  (HashMap<String, Object>) userProfile.get("profileDetails");
                                HashMap<String, Object> personalDetails = (HashMap<String, Object>)profileDetails.get("personalDetails");
                                record = new HashMap<>();
                                record.put("wid", userProfile.get("userId"));
                                record.put("first_name", personalDetails.get("firstname"));
                                record.put("last_name", personalDetails.get("surname"));
                                record.put("email", personalDetails.get("primaryEmail"));
                                userResult.put(record.get("wid").toString(), record);
                            }
                        }
                    }
                }
            }
            logger.info("user result : {}", userResult);
        } catch (Exception e) {
            logger.error(e);
            throw new ApplicationException("Some error occurred while fetching the user details: ", e);
        }
        return userResult;
    }

    private Map<String, Object> getSearchObject(Set<String> userIds) {
        Map<String, Object> request = new HashMap<>();
        Map<String, Object> filters = new HashMap<>();
        filters.put("userId", userIds);
        request.put("filters", filters);
        request.put("query", "");
        Map<String, Object> requestWrapper = new HashMap<>();
        requestWrapper.put("request", request);
        return requestWrapper;
    }

}
