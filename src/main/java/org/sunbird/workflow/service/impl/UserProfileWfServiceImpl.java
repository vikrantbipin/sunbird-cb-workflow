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
        HashMap<String, Object> userResult = new HashMap<>();
        Set<String> userIds = wfInfos.keySet();
        if(!CollectionUtils.isEmpty(userIds)){
            List<String> sources = new ArrayList<>(Constants.USER_DEFAULT_FIELDS);
            if (!sources.contains(Constants.UUID)) {
                sources.add(Constants.UUID);
            }
            Map<String, Object> pidRequestMap = new HashMap<>();
            pidRequestMap.put("source_fields", sources);
            pidRequestMap.put("values", userIds);
            Map<String, String> conditions = new HashMap<>();
            conditions.put("root_org", rootOrg);
            pidRequestMap.put("conditions", conditions);
            try {
                StringBuilder builder = new StringBuilder();
                builder.append(configuration.getPidServiceHost()).append(configuration.getMultipleSearchEndPoint());
                List<Map<String, Object>> pidResponse = (List<Map<String, Object>>) requestServiceImpl.fetchResult(builder, pidRequestMap, List.class);
                logger.info("PID SERVICE RESPONSE : {}", mapper.writeValueAsString(pidResponse));
                for (Map<String, Object> record : pidResponse) {
                    if (record.get("wid") != null && userIds.contains(record.get("wid"))) {
                        userResult.put(record.get("wid").toString(), record);
                    }
                }
            } catch (Exception e) {
                logger.error("PID ERROR", e);
                throw new ApplicationException("PID ERROR: ", e);
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
