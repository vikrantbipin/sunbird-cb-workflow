package com.igot.workflow.service;

import com.igot.workflow.models.Response;
import com.igot.workflow.models.WfRequest;
import com.igot.workflow.postgres.entity.WfStatusEntity;

import java.util.List;
import java.util.Map;

public interface UserProfileWfService {

    public Response updateUserProfile(String rootOrg, String org, WfRequest wfRequest);

    public List<Map<String, Object>> enrichUserData(List<WfStatusEntity> statusEntities, String rootOrg);
}
