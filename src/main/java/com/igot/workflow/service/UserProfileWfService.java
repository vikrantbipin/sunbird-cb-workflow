package com.igot.workflow.service;

import com.igot.workflow.models.WfRequest;
import com.igot.workflow.postgres.entity.WfStatusEntity;

import java.util.List;
import java.util.Map;

public interface UserProfileWfService {

    public void updateUserProfile(WfRequest wfRequest);

    public List<Map<String, Object>> enrichUserData(Map<String, List<WfStatusEntity>> statusEntities, String rootOrg);
}
