package org.sunbird.workflow.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.sunbird.workflow.models.SBApiResponse;
import org.sunbird.workflow.models.WfRequest;
import org.sunbird.workflow.service.SignUpWFService;

@RestController
@RequestMapping("/v1/signup/workflow")
public class SignUpSupportController {

    @Autowired
    SignUpWFService signUpWFService;

    @PostMapping("/create")
    public ResponseEntity<?> wfCreate(@RequestHeader String rootOrg, @RequestHeader String org,
                                      @RequestBody WfRequest wfRequest) {
        SBApiResponse response = signUpWFService.createWorkflow(rootOrg, org, wfRequest);
        return new ResponseEntity<>(response, response.getResponseCode());
    }

    @PostMapping("/update")
    public ResponseEntity<?> wfTransition(@RequestHeader String rootOrg, @RequestHeader String org,
                                          @RequestBody WfRequest wfRequest) {
        SBApiResponse response = signUpWFService.updateWorkflow(rootOrg, org, wfRequest);
        return new ResponseEntity<>(response, response.getResponseCode());
    }

    @GetMapping(path = "/{wfId}/{applicationId}/read", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getWfApplication(@RequestHeader String rootOrg, @RequestHeader String org,
                                              @PathVariable("wfId") String wfId, @PathVariable("applicationId") String applicationId) {
        SBApiResponse response = signUpWFService.getWfApplication(rootOrg, org, wfId, applicationId);
        return new ResponseEntity<>(response, response.getResponseCode());
    }
}
