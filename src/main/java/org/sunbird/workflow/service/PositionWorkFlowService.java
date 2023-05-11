package org.sunbird.workflow.service;

import org.sunbird.workflow.models.Response;
import org.sunbird.workflow.models.SearchCriteria;
import org.sunbird.workflow.models.WfRequest;

public interface PositionWorkFlowService {

    public Response createPositionWorkFlow(String rootOrg, String org, WfRequest wfRequest);
    public Response updatePositionWorkFlow(String rootOrg, String org, WfRequest wfRequest);
    public Response readPositionWFApplication(String rootOrg, String org, String wfId, String applicationId);
    public Response positionSearch(String rootOrg, String org, SearchCriteria criteria);
}
