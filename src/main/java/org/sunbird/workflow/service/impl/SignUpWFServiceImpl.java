package org.sunbird.workflow.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.sunbird.workflow.config.Configuration;
import org.sunbird.workflow.config.Constants;
import org.sunbird.workflow.models.*;
import org.sunbird.workflow.postgres.entity.WfStatusEntity;
import org.sunbird.workflow.postgres.repo.WfStatusRepo;
import org.sunbird.workflow.producer.Producer;
import org.sunbird.workflow.service.SignUpWFService;

import java.io.IOException;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SignUpWFServiceImpl implements SignUpWFService {

    @Autowired
    private WfStatusRepo wfStatusRepo;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private Configuration configuration;

    @Autowired
    private RequestServiceImpl requestServiceImpl;

    @Autowired
    private WorkflowServiceImpl wfService;

    @Autowired
    private Producer producer;

    Logger log = LogManager.getLogger(WorkflowServiceImpl.class);

    @Override
    public SBApiResponse upsertWorkFlow(String rootOrg, String org, WfRequest wfRequest) {
        SBApiResponse response = createDefaultResponse(Constants.SIGNUP_SUPPORT_WORKFLOW_CREATE);
        String errMsg = validateWfRequest(wfRequest);
        if (!StringUtils.isEmpty(errMsg)) {
            response.getParams().setErrmsg(errMsg);
            response.getParams().setStatus(Constants.FAILED);
            response.setResponseCode(HttpStatus.BAD_REQUEST);
            return response;
        }
        response = changeStatus(rootOrg, org, wfRequest, response);
        errMsg = (String) response.getResult().get(Constants.ERROR_MESSAGE);
        if (StringUtils.isNotEmpty(errMsg)) {
            response.getParams().setStatus(Constants.FAILED);
            response.getParams().setErrmsg(errMsg);
            response.setResponseCode(HttpStatus.BAD_REQUEST);
        } else {
            response.getParams().setStatus(Constants.SUCCESSFUL);
            response.setResponseCode(HttpStatus.OK);
        }
        return response;
    }

    private String validateWfRequest(WfRequest wfRequest) {
        List<String> params = new ArrayList<String>();
        StringBuilder strBuilder = new StringBuilder();
        if (StringUtils.isEmpty(wfRequest.getUserId())) {
            params.add(Constants.USER_UUID_VALIDATION_ERROR);
        }
        if (StringUtils.isEmpty(wfRequest.getActorUserId())) {
            params.add(Constants.ACTOR_UUID_VALIDATION_ERROR);
        }
        if (StringUtils.isEmpty(wfRequest.getUserId())) {
            params.add(Constants.APPLICATION_ID_VALIDATION_ERROR);
        }
        if (StringUtils.isEmpty(wfRequest.getState())) {
            params.add(Constants.STATE_VALIDATION_ERROR);
        }
        if (CollectionUtils.isEmpty(wfRequest.getUpdateFieldValues())) {
            params.add(Constants.FIELD_VALUE_VALIDATION_ERROR);
        }
        if (StringUtils.isEmpty(wfRequest.getAction())) {
            params.add(Constants.ACTION_VALIDATION_ERROR);
        }
        if (StringUtils.isEmpty(wfRequest.getServiceName())) {
            params.add(Constants.WORKFLOW_SERVICENAME_VALIDATION_ERROR);
        }
        if (!params.isEmpty()) {
            strBuilder.append("Invalid Request. " + params);
        }
        return strBuilder.toString();
    }

    private SBApiResponse changeStatus(String rootOrg, String org, WfRequest wfRequest, SBApiResponse response) {
        String wfId = wfRequest.getWfId();
        String nextState = null;
        String errMsg = null;
        try {
            WorkFlowModel workFlowModel = wfService.getWorkFlowConfig(wfRequest.getServiceName());
            WfStatus wfStatus = wfService.getWfStatus(wfRequest.getState(), workFlowModel);
            if (wfStatus == null) {
                response.getParams().setErrmsg(Constants.WORKFLOW_STATE_CHECK_ERROR);
                return response;
            }

            WfAction wfAction = wfService.getWfAction(wfRequest.getAction(), wfStatus);
            if (wfAction == null) {
                response.getParams().setErrmsg(Constants.WORKFLOW_ACTION_ERROR);
                return response;
            }
            nextState = wfAction.getNextState();

            WfStatus wfNextStatus = wfService.getWfStatus(nextState, workFlowModel);
            if (wfNextStatus == null) {
                response.getParams().setErrmsg(Constants.WORKFLOW_STATE_CHECK_ERROR);
                return response;
            }
            WfStatusEntity applicationStatus = wfStatusRepo.findByApplicationIdAndWfId(wfRequest.getApplicationId(), wfRequest.getWfId());
            errMsg = validateUserAndWfStatus(wfRequest, wfStatus, applicationStatus);
            if (StringUtils.isEmpty(errMsg)) {
                if (ObjectUtils.isEmpty(applicationStatus)) {
                    applicationStatus = new WfStatusEntity();
                    wfId = UUID.randomUUID().toString();
                    applicationStatus.setWfId(wfId);
                    applicationStatus.setServiceName(wfRequest.getServiceName());
                    applicationStatus.setUserId(wfRequest.getUserId());
                    applicationStatus.setApplicationId(wfRequest.getApplicationId());
                    applicationStatus.setRootOrg(rootOrg);
                    applicationStatus.setOrg(org);
                    applicationStatus.setCreatedOn(new Date());
                    wfRequest.setWfId(wfId);
                }
                applicationStatus.setLastUpdatedOn(new Date());
                applicationStatus.setCurrentStatus(nextState);
                applicationStatus.setActorUUID(wfRequest.getActorUserId());
                applicationStatus.setUpdateFieldValues(mapper.writeValueAsString(wfRequest.getUpdateFieldValues()));
                applicationStatus.setInWorkflow(!wfNextStatus.getIsLastState());
                applicationStatus.setDeptName(wfRequest.getDeptName());
                wfStatusRepo.save(applicationStatus);
                if (wfStatus.getNotificationEnable() == Boolean.TRUE) {
                    producer.push(configuration.getWorkFlowNotificationTopic(), wfRequest);
                }
                producer.push(configuration.getWorkflowApplicationTopic(), wfRequest);
            }
        } catch (IOException e) {
            errMsg = Constants.WORKFLOW_PARSING_ERROR_MESSAGE;
            log.error(errMsg);
        }
        response.getParams().setErrmsg(errMsg);
        HashMap<String, Object> data = new HashMap<>();
        data.put(Constants.WF_ID_CONSTANT, wfId);
        data.put(Constants.STATUS, nextState);
        data.put(Constants.MESSAGE, Constants.STATUS_CHANGE_MESSAGE + nextState);
        response.getResult().putAll(data);
        return response;
    }

    private String validateUserAndWfStatus(WfRequest wfRequest, WfStatus wfStatus, WfStatusEntity applicationStatus) {
        List<String> params = new ArrayList<String>();
        StringBuilder strBuilder = new StringBuilder();
        if (StringUtils.isEmpty(wfRequest.getWfId()) && !wfStatus.getStartState()) {
            params.add(Constants.WORKFLOW_ID_ERROR_MESSAGE);
        }

        if ((!ObjectUtils.isEmpty(applicationStatus))
                && (!wfRequest.getState().equalsIgnoreCase(applicationStatus.getCurrentStatus()))) {
            params.add("Application is in " + applicationStatus.getCurrentStatus()
                    + " State but trying to be move in " + wfRequest.getState() + " state!");
        }
        if (!params.isEmpty()) {
            strBuilder.append(params);
        }
        return strBuilder.toString();
    }

    public SBApiResponse getWfApplication(String rootOrg, String org, String wfId, String applicationId) {
        SBApiResponse response = createDefaultResponse(Constants.SIGNUP_SUPPORT_WORKFLOW_READ);
        WfStatusEntity applicationStatus = wfStatusRepo.findByRootOrgAndOrgAndApplicationIdAndWfId(rootOrg, org,
                applicationId, wfId);
        List<Map<String, Object>> updateFieldValuesList = null;
        String errMsg = null;
        if (applicationStatus != null) {
            if (StringUtils.isNotEmpty(applicationStatus.getUpdateFieldValues())) {
                try {
                    updateFieldValuesList = mapper.readValue(applicationStatus.getUpdateFieldValues(), new TypeReference<List<Map<String, Object>>>() {
                    });
                } catch (Exception ex) {
                    errMsg = Constants.UPDATED_FIELD_VALUES_ERROR_MESSAGE;
                    log.error(errMsg);
                }
            }
        } else {
            errMsg = Constants.WF_NOT_AVAILABLE;
        }
        if (StringUtils.isNotBlank(errMsg)) {
            response.getParams().setStatus(Constants.FAILED);
            response.getParams().setErrmsg(errMsg);
            response.setResponseCode(HttpStatus.BAD_REQUEST);
            return response;
        }
        if (applicationStatus != null) {
            Map<String, Object> wfMap = Arrays.stream(applicationStatus.getClass().getDeclaredFields())
                    .peek(field -> field.setAccessible(true))
                    .collect(Collectors.toMap(Field::getName, field -> {
                        try {
                            Object value = field.get(applicationStatus);
                            return value != null ? value : "null";
                        } catch (IllegalAccessException e) {
                            log.error("Error accessing field: " + field.getName());
                            return null;
                        }
                    }));
            wfMap.put(Constants.UPDATED_FIELDS_VALUES, updateFieldValuesList);
            response.getResult().putAll(wfMap);
        }
        response.getParams().setStatus(Constants.SUCCESSFUL);
        response.setResponseCode(HttpStatus.OK);
        return response;
    }

    private SBApiResponse createDefaultResponse(String api) {
        SBApiResponse response = new SBApiResponse();
        response.setId(api);
        response.setVer(Constants.API_VERSION_1);
        response.setParams(new SunbirdApiRespParam());
        response.getParams().setStatus(Constants.SUCCESS);
        response.setResponseCode(HttpStatus.OK);
        response.setTs(LocalDateTime.now().toString());
        return response;
    }
}
