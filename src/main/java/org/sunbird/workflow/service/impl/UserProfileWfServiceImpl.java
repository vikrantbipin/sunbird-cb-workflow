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
        HashMap<String, Object> userResult = new HashMap<>();
        Set<String> userIds = wfInfos.keySet();
        if (!CollectionUtils.isEmpty(userIds)) {
            List<String> sources = new ArrayList<>(Constants.USER_DEFAULT_FIELDS);
            if (!sources.contains(Constants.UUID)) {
                sources.add(Constants.UUID);
            }
            Map<String, Object> request = new HashMap<>();
            Map<String, Object> filters = new HashMap<>();
            Map<String, Object> idKeyword = new HashMap<>();
            idKeyword.put("or", userIds);
            filters.put("id.keyword", idKeyword);
            request.put("limit", userIds.size());
            request.put("offset", 0);
            request.put("filters", filters);
            Map<String, Object> record;
            HashMap<String, String> headersValue = new HashMap<>();
            headersValue.put("Content-Type", "application/json");
            try {
                StringBuilder builder = new StringBuilder();
                builder.append(configuration.getHubServiceHost()).append(configuration.getHubProfileSearchEndPoint());
                Map<String, Object> openSaberApiResp = (Map<String, Object>) requestServiceImpl.fetchResultUsingPost(builder, request, Map.class, headersValue);
                if (openSaberApiResp != null && "OK".equalsIgnoreCase((String) openSaberApiResp.get("responseCode"))) {
                    Map<String, Object> map = (Map<String, Object>) openSaberApiResp.get("result");
                    List<Map<String, Object>> userProfiles = (List<Map<String, Object>>) map.get("UserProfile");
                    if (!CollectionUtils.isEmpty(userProfiles)) {
                        for (Map<String, Object> userProfile : userProfiles) {
                            HashMap<String, Object> personalDetails = (HashMap<String, Object>) userProfile.get("personalDetails");
                            record = new HashMap<>();
                            record.put("wid", userProfile.get("userId"));
                            record.put("first_name", personalDetails.get("firstname"));
                            record.put("last_name", personalDetails.get("surname"));
                            record.put("email", personalDetails.get("primaryEmail"));
                            userResult.put(record.get("wid").toString(), record);
                        }
                    }
                }
            } catch (Exception e) {
                logger.error(e);
                throw new ApplicationException("Hub Service ERROR: ", e);
            }
        }
        for (Map.Entry<String, List<WfStatusEntity>> wfStatusEntity : wfInfos.entrySet()) {
            responseMap = new HashMap<>();
            responseMap.put("wfInfo", wfStatusEntity.getValue());
            responseMap.put("userInfo", userResult.get(wfStatusEntity.getKey()));
            wfDetails.add(responseMap);
        }
        return wfDetails;
    }
}
