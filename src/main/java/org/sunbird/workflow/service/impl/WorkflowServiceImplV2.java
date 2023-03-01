package org.sunbird.workflow.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.sunbird.workflow.models.Response;
import org.sunbird.workflow.models.SearchCriteria;
import org.sunbird.workflow.models.WfRequest;
import org.sunbird.workflow.service.WorkflowServiceV2;

@Service
public class WorkflowServiceImplV2 implements WorkflowServiceV2 {

	@Override
	public Response workflowTransition(String userToken, WfRequest wfRequest) {

		return null;
	}

	@Override
	public Response getWfApplication(String userToken, String wfId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response wfApplicationSearch(String userToken, SearchCriteria criteria) {
		// TODO Auto-generated method stub
		return null;
	}

	private String validateRequest(WfRequest wfRequest) {
		String errorMsg = "";

		if (StringUtils.isEmpty(wfRequest.getWfId())) {
			// Status should be initial.
		} else {
			// Status must not be initial.
		}
		// check for other parameters based on requirement.
		return errorMsg;
	}
}
