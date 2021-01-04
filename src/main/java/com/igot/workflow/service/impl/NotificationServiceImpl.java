package com.igot.workflow.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.igot.workflow.config.Configuration;
import com.igot.workflow.consumer.ApplicationProcessingConsumer;
import com.igot.workflow.models.WfRequest;
import com.igot.workflow.models.WfStatus;
import com.igot.workflow.models.WorkFlowModel;
import com.igot.workflow.models.cassandra.Workflow;
import com.igot.workflow.models.notification.NotificationEvent;
import com.igot.workflow.postgres.entity.WfStatusEntity;
import com.igot.workflow.postgres.repo.WfStatusRepo;
import com.igot.workflow.repository.cassandra.bodhi.WfRepo;
import com.igot.workflow.service.Workflowservice;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.*;

@Service
public class NotificationServiceImpl {

    Logger logger = LogManager.getLogger(ApplicationProcessingConsumer.class);

    @Autowired
    private WfStatusRepo wfStatusRepo;

    @Autowired
    private Configuration configuration;

    @Autowired
    private RequestServiceImpl requestService;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private Workflowservice workflowservice;

    @Autowired
    private WfRepo wfRepo;

    private static final String WORK_FLOW_EVENT_NAME = "workflow_service_notification";

    private static final String USER_NAME_CONSTANT = "user";

    private static final String USER_NAME_TAG = "#userName";

    private static final String STATE_NAME_TAG = "#state";

    private static final String FIELD_KEY_TAG = "#fieldKey";

    private static final String TO_VALUE_TAG = "#toValue";

    private static final String TO_VALUE_CONST= "toValue";

    /**
     * Send notification to the user based on state of application
     * @param wfRequest workflow request
     */
    public void sendNotification(WfRequest wfRequest){
        WfStatusEntity wfStatusEntity = wfStatusRepo.findByApplicationIdAndWfId(wfRequest.getApplicationId(), wfRequest.getWfId());
        WfStatus wfStatus = workflowservice.getWorkflowStates(wfStatusEntity.getRootOrg(), wfStatusEntity.getOrg(), wfStatusEntity.getServiceName(), wfStatusEntity.getCurrentStatus());
        try {
            logger.info("Notification workflow status entity, {}", mapper.writeValueAsString(wfStatusEntity));
            logger.info("Notification workflow status model, {}", mapper.writeValueAsString(wfStatus));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        if(!ObjectUtils.isEmpty(wfStatus.getNotificationEnable()) && wfStatus.getNotificationEnable()){
            logger.info("Enter's in the notification block");
            Map<String, Object> tagValues = new HashMap<>();
            Map<String, List<String>> recipients = new HashMap<>();
            NotificationEvent nEvent = new NotificationEvent();
            nEvent.setEventId(WORK_FLOW_EVENT_NAME);
            tagValues.put(USER_NAME_TAG, wfRequest.getUserId());
            tagValues.put(STATE_NAME_TAG, wfStatusEntity.getCurrentStatus());
            Optional<HashMap<String, Object>> updatedFieldValue = wfRequest.getUpdateFieldValues().stream().findFirst();
            if(updatedFieldValue.isPresent()){
                HashMap<String, Object> toValue = (HashMap<String, Object>)updatedFieldValue.get().get(TO_VALUE_CONST);
                tagValues.put(FIELD_KEY_TAG, toValue.entrySet().iterator().next().getKey());
                tagValues.put(TO_VALUE_TAG, toValue.entrySet().iterator().next().getValue());
            }
            nEvent.setTagValues(tagValues);
            nEvent.setRecipients(recipients);
            List<String> userUUID = Collections.singletonList(wfRequest.getUserId());
            recipients.put(USER_NAME_CONSTANT, userUUID);
            sendNotification(nEvent);
        }
    }

    /**
     * Post to the Notification service
     *
     * @param nEvent
     * @throws Exception
     */
    public void sendNotification(NotificationEvent nEvent) {
        StringBuilder builder = new StringBuilder();
        builder.append(configuration.getNotifyServiceHost()).append(configuration.getNotifyServicePath());
        try {
            requestService.fetchResultUsingPost(builder, nEvent, Map.class);
        } catch (Exception e) {
            logger.error("Exception while posting the data in notification service: ", e);
        }

    }
}
