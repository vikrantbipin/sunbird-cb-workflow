package com.igot.workflow.service.impl;


import com.igot.workflow.config.Configuration;
import com.igot.workflow.config.Constants;
import com.igot.workflow.models.Response;
import com.igot.workflow.models.WfRequest;
import com.igot.workflow.models.WfStatus;
import com.igot.workflow.service.UserProfileWfService;
import com.igot.workflow.service.Workflowservice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserProfileWfServiceImpl implements UserProfileWfService {

    @Autowired
    private Workflowservice workflowservice;

    @Autowired
    private RequestService requestService;

    @Autowired
    private Configuration configuration;

    private static final String FIELD_KEY= "fieldKey";

    private static final String FIELD_TO_VALUE= "toValue";

    public Response updateUserProfile(String rootOrg, String org, WfRequest wfRequest) {
        Response response = workflowservice.statusChange(rootOrg, org, wfRequest);
        String status = (String) response.get(Constants.DATA);
        WfStatus state = workflowservice.getWorkflowStates(rootOrg, org, wfRequest.getServiceName(), status);
        if (Constants.APPROVED_STATE.equals(state.getState())) {
            Map<String, Object> requestValue = new HashMap<>();
            for (HashMap<String, Object> updatedValues : wfRequest.getUpdateFieldValues()) {
                requestValue.put((String) updatedValues.get(FIELD_KEY), updatedValues.get(FIELD_TO_VALUE));
            }
            StringBuilder builder = new StringBuilder();
            String endPoint = configuration.getHubProfileUpdateEndPoint().replace(Constants.USER_ID_VALUE, wfRequest.getApplicationId());
            builder.append(configuration.getHubServiceHost()).append(endPoint);
            requestService.fetchResult(builder, requestValue);
       }
        return response;
    }
}
