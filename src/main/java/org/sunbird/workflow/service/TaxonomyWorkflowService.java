package org.sunbird.workflow.service;

import org.sunbird.workflow.models.SBApiResponse;
import org.sunbird.workflow.models.SearchCriteria;
import org.sunbird.workflow.models.WfRequest;

public interface TaxonomyWorkflowService {
	public SBApiResponse createWorkflow(String userToken, WfRequest wfRequest);
	public SBApiResponse updateWorkflow(String userToken, WfRequest wfRequest);

	public SBApiResponse getWfApplication(String userToken, String wfId);

	public SBApiResponse wfApplicationSearch(String userToken, SearchCriteria criteria);
}
