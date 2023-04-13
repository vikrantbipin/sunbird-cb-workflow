package org.sunbird.workflow.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.sunbird.workflow.models.Response;
import org.sunbird.workflow.models.WfRequest;
import org.sunbird.workflow.service.Workflowservice;

@RestController
@RequestMapping("/v1/signup/workflow")
public class SignUpWorkFlowController {

    @Autowired
    Workflowservice workflowservice;

    @PostMapping("/create")
    public ResponseEntity<Response> wfCreate(@RequestHeader String rootOrg, @RequestHeader String org,
                                      @RequestBody WfRequest wfRequest) {
        Response response = workflowservice.workflowTransition(rootOrg, org, wfRequest);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/update")
    public ResponseEntity<Response> wfUpdate(@RequestHeader String rootOrg, @RequestHeader String org,
                                          @RequestBody WfRequest wfRequest) {
        Response response = workflowservice.workflowTransition(rootOrg, org, wfRequest);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping(path = "/{wfId}/{applicationId}/read", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Response> getWfApplication(@RequestHeader String rootOrg, @RequestHeader String org,
                                              @PathVariable("wfId") String wfId, @PathVariable("applicationId") String applicationId) {
        Response response = workflowservice.getWfApplication(rootOrg, org, wfId, applicationId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
