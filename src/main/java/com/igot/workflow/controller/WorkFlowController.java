package com.igot.workflow.controller;

import com.igot.workflow.models.Response;
import com.igot.workflow.models.SearchCriteria;
import com.igot.workflow.models.WfRequest;
import com.igot.workflow.service.Workflowservice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/workflow")
public class WorkFlowController {

    @Autowired
    private Workflowservice workflowService;

    @PostMapping("/transition")
    public ResponseEntity<Response> WfTransition(@RequestHeader String rootOrg, @RequestHeader String org, @RequestBody WfRequest wfRequest) {
        Response response = workflowService.workflowTransition(rootOrg, org, wfRequest);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping(path = "/{wfId}/{applicationId}/status", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Response> getWfApplication(@RequestHeader String rootOrg, @RequestHeader String org, @PathVariable("wfId") String wfId, @PathVariable("applicationId") String applicationId) {
        Response response = workflowService.getWfApplication(rootOrg, org, wfId, applicationId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping(path = "/applications/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Response> wfApplicationSearch(@RequestHeader String rootOrg, @RequestHeader String org, @RequestBody SearchCriteria searchCriteria) {
        Response response = workflowService.applicationsSearch(rootOrg, org, searchCriteria);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping(path = "/nextAction/{serviceName}/{state}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Response> getNextActionForState(@RequestHeader String rootOrg, @RequestHeader String org,@PathVariable("serviceName") String serviceName , @PathVariable("state") String state) {
        Response response = workflowService.getNextActionForState(rootOrg, org, serviceName, state);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping(path = "/{wfId}/{applicationId}/history", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Response> getApplicationHistoryOnWfId(@RequestHeader String rootOrg, @PathVariable("wfId") String wfId, @PathVariable("applicationId") String applicationId) {
        Response response = workflowService.getApplicationHistoryOnWfId(rootOrg, wfId, applicationId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping(path = "/{applicationId}/history", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Response> getApplicationWfHistory(@RequestHeader String rootOrg, @PathVariable("applicationId") String applicationId) {
        Response response = workflowService.getApplicationWfHistory(rootOrg, applicationId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
