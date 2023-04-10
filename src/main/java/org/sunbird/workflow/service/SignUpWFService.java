package org.sunbird.workflow.service;

import org.sunbird.workflow.models.SBApiResponse;
import org.sunbird.workflow.models.SearchCriteria;
import org.sunbird.workflow.models.WfRequest;

public interface SignUpWFService {
    public SBApiResponse createWorkflow(String rootOrg, String org, WfRequest wfRequest);

    public SBApiResponse updateWorkflow(String rootOrg, String org, WfRequest wfRequest);

    public SBApiResponse getWfApplication(String rootOrg, String org, String wfId, String applicationId);
}
