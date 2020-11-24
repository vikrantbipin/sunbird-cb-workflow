package com.igot.workflow.service;

import com.igot.workflow.models.Response;
import com.igot.workflow.models.WfRequest;

public interface UserProfileWfService {

    public Response updateUserProfile(String rootOrg, String org, WfRequest wfRequest);
}
