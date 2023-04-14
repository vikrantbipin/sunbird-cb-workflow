package org.sunbird.workflow.service;

import org.sunbird.workflow.models.Response;
import org.sunbird.workflow.models.SearchCriteria;
import org.sunbird.workflow.models.WfRequest;

public interface OrganisationWorkFlowService {
    public Response createOrgWorkFlow(String rootOrg, String org, WfRequest wfRequest);

    public Response updateOrgWorkFlow(String rootOrg, String org, WfRequest wfRequest);

    public Response readOrgWFApplication(String rootOrg, String org, String wfId, String applicationId);

    public Response orgSearch(String rootOrg, String org, SearchCriteria criteria);
}
