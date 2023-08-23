package org.sunbird.workflow.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.sunbird.workflow.config.Configuration;
import org.sunbird.workflow.config.Constants;
import org.sunbird.workflow.exception.InvalidDataInputException;
import org.sunbird.workflow.models.Response;
import org.sunbird.workflow.models.SearchCriteria;
import org.sunbird.workflow.models.WfRequest;
import org.sunbird.workflow.postgres.entity.WfStatusEntity;
import org.sunbird.workflow.postgres.repo.WfStatusRepo;
import org.sunbird.workflow.producer.Producer;
import org.sunbird.workflow.service.BPWorkFlowService;
import org.sunbird.workflow.service.Workflowservice;
import org.sunbird.workflow.utils.CassandraOperation;

import java.util.*;

@Service
public class BPWorkFlowServiceImpl implements BPWorkFlowService {

    private Logger logger = LoggerFactory.getLogger(BPWorkFlowServiceImpl.class);

    @Autowired
    private Workflowservice workflowService;

    @Autowired
    private CassandraOperation cassandraOperation;

    @Autowired
    private RequestServiceImpl requestServiceImpl;

    @Autowired
    private Configuration configuration;

    @Autowired
	private WfStatusRepo wfStatusRepo;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private Producer producer;

    @Override
    public Response enrolBPWorkFlow(String rootOrg, String org, WfRequest wfRequest) {
        Map<String, Object> courseBatchDetails = getCurrentBatchAttributes(wfRequest.getApplicationId(), wfRequest.getCourseId());
        int totalUserEnrolCount = getTotalUserEnrolCount(wfRequest);
        boolean enrolAccess = validateBatchEnrolment(courseBatchDetails, totalUserEnrolCount);
        if (!enrolAccess) {
            Response response = new Response();
            response.put(Constants.ERROR_MESSAGE, "BATCH_IS_FULL");
            response.put(Constants.STATUS,HttpStatus.BAD_REQUEST);
            return response;
        }
        Response response = workflowService.workflowTransition(rootOrg, org, wfRequest);
        return response;
    }

    @Override
    public Response updateBPWorkFlow(String rootOrg, String org, WfRequest wfRequest) {
        Response response = workflowService.workflowTransition(rootOrg, org, wfRequest);
        return response;
    }

    @Override
    public Response readBPWFApplication(String rootOrg, String org, String wfId, String applicationId) {
        Response response = workflowService.getWfApplication(rootOrg, org, wfId, applicationId);
        return response;
    }

    @Override
    public Response blendedProgramSearch(String rootOrg, String org, SearchCriteria criteria) {
        Response response = workflowService.applicationsSearch(rootOrg, org, criteria, Constants.BLENDED_PROGRAM_SEARCH_ENABLED);
        return response;
    }

    @Override
    public void updateEnrolmentDetails(WfRequest wfRequest) {
        Map<String, Object> courseBatchDetails = getCurrentBatchAttributes(wfRequest.getApplicationId(), wfRequest.getCourseId());
        int totalUserEnrolCount = getTotalUserEnrolCount(wfRequest);
        boolean enrolAccess = validateBatchEnrolment(courseBatchDetails, totalUserEnrolCount);
        if (enrolAccess) {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put(Constants.USER_ID, wfRequest.getUserId());
            requestBody.put(Constants.BATCH_ID, wfRequest.getApplicationId());
            requestBody.put(Constants.COURSE_ID, wfRequest.getCourseId());
            Map<String, Object> request = new HashMap<>();
            request.put(Constants.REQUEST,requestBody);
            HashMap<String, String> headersValue = new HashMap<>();
            headersValue.put("Content-Type", "application/json");
            try {
                StringBuilder builder = new StringBuilder(configuration.getCourseServiceHost());
                builder.append(configuration.getAdminEnrolEndPoint());
                Map<String, Object> enrolResp = (Map<String, Object>) requestServiceImpl
                        .fetchResultUsingPost(builder, request, Map.class, headersValue);
                if (enrolResp != null
                        && "OK".equalsIgnoreCase((String) enrolResp.get(Constants.RESPONSE_CODE))) {
                    logger.info("User enrolment success");
                } else {
                    logger.error("user enrolment failed" + ((Map<String, Object>) enrolResp.get(Constants.PARAMS)).get(Constants.ERROR_MESSAGE));
                }
            } catch (Exception e) {
                logger.error("Exception while enrol user");
            }
        }
    }

    private Map<String, Object> getCurrentBatchAttributes(String batchId, String courseId) {
        Map<String, Object> propertyMap = new HashMap<>();
        propertyMap.put(Constants.BATCH_ID, batchId);
        propertyMap.put(Constants.COURSE_ID, courseId);
        List<Map<String, Object>> batchAttributesDetails = cassandraOperation.getRecordsByProperties(
                Constants.KEYSPACE_SUNBIRD_COURSES,
                Constants.TABLE_COURSE_BATCH,
                propertyMap,
                Arrays.asList(Constants.BATCH_ATTRIBUTES, Constants.ENROLMENT_END_DATE)
        );
        return batchAttributesDetails.stream()
                .findFirst()
                .map(Optional::ofNullable)
                .orElse(Optional.empty())
                .map(details -> {
                    Map<String, Object> batchAttributes = (Map<String, Object>) details.get(Constants.BATCH_ATTRIBUTES);
                    String currentBatchSizeString = batchAttributes != null && batchAttributes.containsKey(Constants.CURRENT_BATCH_SIZE)
                            ? (String) batchAttributes.get(Constants.CURRENT_BATCH_SIZE)
                            : "0";
                    int currentBatchSize = Integer.parseInt(currentBatchSizeString);
                    Date enrollmentEndDate = details.containsKey(Constants.ENROLMENT_END_DATE)
                            ? (Date) details.get(Constants.ENROLMENT_END_DATE)
                            : null;
                    Map<String, Object> result = new HashMap<>();
                    result.put(Constants.CURRENT_BATCH_SIZE, currentBatchSize);
                    result.put(Constants.ENROLMENT_END_DATE, enrollmentEndDate);
                    return result;
                })
                .orElse(Collections.emptyMap());
    }


    private int getTotalUserEnrolCount(WfRequest wfRequest) {
        Map<String, Object> propertyMap = new HashMap<>();
        propertyMap.put(Constants.BATCH_ID, wfRequest.getApplicationId());
        int totalCount = cassandraOperation.getCountByProperties(Constants.KEYSPACE_SUNBIRD_COURSES, Constants.TABLE_ENROLMENT_BATCH_LOOKUP, propertyMap);
        return totalCount;
    }

    private boolean validateBatchEnrolment(Map<String, Object> courseBatchDetails, int totalUserEnrolCount) {
        if (MapUtils.isEmpty(courseBatchDetails)) {
            return false;
        }
        int currentBatchSize = 0;
        if (courseBatchDetails.containsKey(Constants.CURRENT_BATCH_SIZE)) {
            currentBatchSize = (int) courseBatchDetails.get(Constants.CURRENT_BATCH_SIZE);
        }
        Date enrollmentEndDate = (Date) courseBatchDetails.get(Constants.ENROLMENT_END_DATE);

        boolean enrolAccess = (totalUserEnrolCount + 1 <= currentBatchSize) && (enrollmentEndDate.after(new Date()));
        return enrolAccess;
    }

    @Override
    public Response blendedProgramUserSearch(String rootOrg, String org, String userId, SearchCriteria searchCriteria) {
        searchCriteria.setUserId(userId);
        Response response = workflowService.applicationsSearch(rootOrg, org, searchCriteria);
        return response;
    }

    public Response readBPWFApplication(String wfId, boolean isPc) {
        WfStatusEntity applicationStatus = wfStatusRepo.findByWfId(wfId);
		List<WfStatusEntity> applicationList = applicationStatus == null ? new ArrayList<>()
				: new ArrayList<>(Arrays.asList(applicationStatus));
		Response response = new Response();
        if (isPc) {
            // TODO - Need to enrich this response with User Profile Details ?
        }
		response.put(Constants.MESSAGE, Constants.SUCCESSFUL);
		response.put(Constants.DATA, applicationList);
		response.put(Constants.STATUS, HttpStatus.OK);
		return response;
    }

    /**
     * Service method to handle the user enrolled by the admin.
     *
     * @param rootOrg   - Root Organization Name ex: "igot"
     * @param org       - Organization name ex: "dopt"
     * @param wfRequest - WorkFlow request which needs to be processed.
     * @return - Return the response of success/failure after processing the request.
     */
    @Override
    public Response adminEnrolBPWorkFlow(String rootOrg, String org, WfRequest wfRequest) {
        Map<String, Object> courseBatchDetails = getCurrentBatchAttributes(wfRequest.getApplicationId(), wfRequest.getCourseId());
        int totalUserEnrolCount = getTotalUserEnrolCount(wfRequest);
        boolean enrolAccess = validateBatchEnrolment(courseBatchDetails, totalUserEnrolCount);
        if (!enrolAccess) {
            Response response = new Response();
            response.put(Constants.ERROR_MESSAGE, "BATCH_IS_FULL");
            response.put(Constants.STATUS, HttpStatus.BAD_REQUEST);
            return response;
        }
        List<WfStatusEntity> enrollmentStatus = wfStatusRepo.findByServiceNameAndUserIdAndApplicationId(wfRequest.getServiceName(), wfRequest.getUserId(), wfRequest.getApplicationId());
        Response response;
        if (!enrollmentStatus.isEmpty()) {
            response = new Response();
            response.put(Constants.MESSAGE, "Not allowed to enroll the user to the Blended Program");
            response.put(Constants.STATUS, HttpStatus.OK);
        } else {
            response = saveAdminEnrollUserIntoWfStatus(rootOrg, org, wfRequest);
            producer.push(configuration.getWorkFlowNotificationTopic(), wfRequest);
            producer.push(configuration.getWorkflowApplicationTopic(), wfRequest);
        }
        return response;
    }

    /**
     * Save Method to save the admin enrolled data into the wf_status table.
     *
     * @param rootOrg   - Root Organization Name ex: "igot"
     * @param org       - Organization name ex: "dopt"
     * @param wfRequest - WorkFlow request which needs to be processed.
     * @return - Return the response of success/failure after processing the request.
     */
    private Response saveAdminEnrollUserIntoWfStatus(String rootOrg, String org, WfRequest wfRequest) {
        validateWfRequest(wfRequest);
        WfStatusEntity applicationStatus = new WfStatusEntity();
        String wfId = UUID.randomUUID().toString();
        applicationStatus.setWfId(wfId);
        applicationStatus.setApplicationId(wfRequest.getApplicationId());
        applicationStatus.setUserId(wfRequest.getUserId());
        applicationStatus.setInWorkflow(true);
        applicationStatus.setServiceName(wfRequest.getServiceName());
        applicationStatus.setActorUUID(wfRequest.getActorUserId());
        applicationStatus.setCreatedOn(new Date());
        applicationStatus.setCurrentStatus(Constants.APPROVED_STATE);
        applicationStatus.setLastUpdatedOn(new Date());
        applicationStatus.setOrg(org);
        applicationStatus.setRootOrg(rootOrg);
        try {
            applicationStatus.setUpdateFieldValues(mapper.writeValueAsString(wfRequest.getUpdateFieldValues()));
        } catch (JsonProcessingException e) {
            logger.error(String.valueOf(e));
        }
        applicationStatus.setDeptName(wfRequest.getDeptName());
        applicationStatus.setComment(wfRequest.getComment());
        wfStatusRepo.save(applicationStatus);

        Response response = new Response();
        HashMap<String, Object> data = new HashMap<>();
        data.put(Constants.STATUS, Constants.APPROVED_STATE);
        data.put(Constants.WF_IDS_CONSTANT, wfId);
        response.put(Constants.MESSAGE, Constants.STATUS_CHANGE_MESSAGE + Constants.APPROVED_STATE);
        response.put(Constants.DATA, data);
        response.put(Constants.STATUS, HttpStatus.OK);
        return response;
    }

    /**
     * @param wfRequest - Validate the fields received in the wfRequest.
     */
    private void validateWfRequest(WfRequest wfRequest) {

        if (StringUtils.isEmpty(wfRequest.getState())) {
            throw new InvalidDataInputException(Constants.STATE_VALIDATION_ERROR);
        }

        if (StringUtils.isEmpty(wfRequest.getApplicationId())) {
            throw new InvalidDataInputException(Constants.APPLICATION_ID_VALIDATION_ERROR);
        }

        if (StringUtils.isEmpty(wfRequest.getActorUserId())) {
            throw new InvalidDataInputException(Constants.ACTOR_UUID_VALIDATION_ERROR);
        }

        if (StringUtils.isEmpty(wfRequest.getUserId())) {
            throw new InvalidDataInputException(Constants.USER_UUID_VALIDATION_ERROR);
        }


        if (CollectionUtils.isEmpty(wfRequest.getUpdateFieldValues())) {
            throw new InvalidDataInputException(Constants.FIELD_VALUE_VALIDATION_ERROR);
        }

        if (StringUtils.isEmpty(wfRequest.getServiceName())) {
            throw new InvalidDataInputException(Constants.WORKFLOW_SERVICENAME_VALIDATION_ERROR);
        }
    }

}
