package org.sunbird.workflow.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.sunbird.workflow.config.Constants;
import org.sunbird.workflow.models.WfRequest;
import org.sunbird.workflow.service.UserProfileWfService;

@Service
public class ApplicationProcessingServiceImpl {

    @Autowired
    private UserProfileWfService userProfileWfService;

    public void processWfApplicationRequest(WfRequest wfRequest) {
        switch (wfRequest.getServiceName()) {
            //Or condition in case statement
            case Constants.PROFILE_SERVICE_NAME:
            case Constants.USER_PROFILE_FLAG_SERVICE:
                userProfileWfService.updateUserProfile(wfRequest);
                break;
            default:
                break;
        }
    }
}
