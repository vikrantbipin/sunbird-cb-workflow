package org.sunbird.workflow.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.sunbird.workflow.models.SBApiResponse;
import org.sunbird.workflow.models.SearchCriteria;
import org.sunbird.workflow.models.WfRequest;
import org.sunbird.workflow.service.TaxonomyWorkflowService;

@RestController
@RequestMapping("/taxonomy/workflow")
public class TaxonomyWorkFlowController {

    @Autowired
    private TaxonomyWorkflowService taxonomyworkflowService;

    @PostMapping("/create")
    public ResponseEntity<?> wfCreate(@RequestHeader String userToken, @RequestBody WfRequest wfRequest) {
        SBApiResponse response = taxonomyworkflowService.createWorkflow(userToken, wfRequest);
        return new ResponseEntity<>(response, response.getResponseCode());
    }

    @PatchMapping("/update")
    public ResponseEntity<?> wfTransition(@RequestHeader String userToken, @RequestBody WfRequest wfRequest) {
        SBApiResponse response = taxonomyworkflowService.updateWorkflow(userToken, wfRequest);
        return new ResponseEntity<>(response, response.getResponseCode());
    }

    @GetMapping(path = "/read/{wfId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getWfApplication(@RequestHeader String userToken, @PathVariable("wfId") String wfId) {
        SBApiResponse response = taxonomyworkflowService.getWfApplication(userToken, wfId);
        return new ResponseEntity<>(response, response.getResponseCode());
    }

    @PostMapping(path = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> wfApplicationSearch(@RequestHeader String userToken, @RequestBody SearchCriteria searchCriteria) {
        SBApiResponse response = taxonomyworkflowService.wfApplicationSearch(userToken, searchCriteria);
        return new ResponseEntity<>(response, response.getResponseCode());
    }
}
