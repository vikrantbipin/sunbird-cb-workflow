package org.sunbird.workflow.service;

import org.sunbird.workflow.models.Response;
import org.sunbird.workflow.models.SearchCriteria;
import org.sunbird.workflow.models.WfRequest;

public interface DomainWhiteListWorkFlowService {

    public Response createWorkFlow(String rootOrg, String org, WfRequest wfRequest);

    public Response updateWorkFlow(String rootOrg, String org, WfRequest wfRequest);

    public Response readWFApplication(String rootOrg, String org, String wfId, String applicationId);

    public Response domainSearch(String rootOrg, String org, SearchCriteria criteria);
}
