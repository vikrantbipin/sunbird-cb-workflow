package com.igot.workflow.service;

import com.igot.workflow.models.Response;
import com.igot.workflow.models.SearchCriteria;
import com.igot.workflow.models.WfRequest;
import com.igot.workflow.models.WfStatus;

public interface Workflowservice {

    public Response workflowTransition(String rootOrg, String org, WfRequest wfRequest);

    public Response statusChange(String rootOrg, String org, WfRequest wfRequest);

    public Response getWfApplication(String rootOrg, String org, String wfId, String applicationId);

    public Response wfApplicationSearch(String rootOrg, String org, SearchCriteria criteria);

    public Response getApplicationHistoryOnWfId(String rootOrg, String wfId, String userId);

    public Response getNextActionForState(String rootOrg, String org, String serviceName, String state);

    public WfStatus getWorkflowStates(String rootOrg, String org, String serviceName, String state);

    public Response getApplicationWfHistory(String rootOrg, String applicationId);

    public Response applicationsSearch(String rootOrg, String org, SearchCriteria criteria);
}
