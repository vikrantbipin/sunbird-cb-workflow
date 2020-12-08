package com.igot.workflow.service.impl;

import com.igot.workflow.config.Constants;
import com.igot.workflow.models.WfRequest;
import com.igot.workflow.service.UserProfileWfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
