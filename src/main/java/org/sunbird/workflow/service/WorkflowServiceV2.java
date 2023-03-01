package org.sunbird.workflow.service;

import org.sunbird.workflow.models.Response;
import org.sunbird.workflow.models.SearchCriteria;
import org.sunbird.workflow.models.WfRequest;

public interface WorkflowServiceV2 {
	public Response workflowTransition(String userToken, WfRequest wfRequest);

	public Response getWfApplication(String userToken, String wfId);

	public Response wfApplicationSearch(String userToken, SearchCriteria criteria);
}
