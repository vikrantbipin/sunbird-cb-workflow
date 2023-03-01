package org.sunbird.workflow.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.sunbird.workflow.models.Response;
import org.sunbird.workflow.models.WfRequest;
import org.sunbird.workflow.service.WorkflowServiceV2;

@RestController
@RequestMapping("/v2/workflow")
public class WorkFlowControllerV2 {

	@Autowired
	private WorkflowServiceV2 workflowService;

	@PostMapping("/transition")
	public ResponseEntity<Response> wfTransition(@RequestHeader String userToken, @RequestBody WfRequest wfRequest) {
		Response response = workflowService.workflowTransition(userToken, wfRequest);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
}
