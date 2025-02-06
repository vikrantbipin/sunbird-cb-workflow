package org.sunbird.workflow.service;

import org.sunbird.workflow.models.WfRequest;

public interface WorkflowESSyncService {
     public void syncWithElasticService(WfRequest wfRequest);
}
