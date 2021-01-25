package com.igot.workflow.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.igot.workflow.models.WfRequest;
import com.igot.workflow.service.impl.ApplicationProcessingServiceImpl;
import com.igot.workflow.service.impl.WorkflowAuditProcessingServiceImpl;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.stereotype.Service;

@Service
public class ApplicationProcessingConsumer {

    Logger logger = LogManager.getLogger(ApplicationProcessingConsumer.class);

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private ApplicationProcessingServiceImpl applicationProcessingServiceImpl;

    @Autowired
    private WorkflowAuditProcessingServiceImpl workflowAuditProcessingService;


    @KafkaListener(id = "id0", groupId = "workflowContentTopic-consumer", topicPartitions = {@TopicPartition(topic = "${kafka.topics.workflow.request}", partitions = {"0", "1", "2", "3"})})
    public void processMessage(ConsumerRecord<String, String> data) {
        WfRequest wfRequest = null;
        try {
            String message = String.valueOf(data.value());
            wfRequest = mapper.readValue(message, WfRequest.class);
            logger.info("Recevied data in user profile consumer : {}", mapper.writeValueAsString(wfRequest));
        } catch (Exception ex) {
            logger.error("Error while deserialization the object value", ex);
        }
        applicationProcessingServiceImpl.processWfApplicationRequest(wfRequest);
        workflowAuditProcessingService.createAudit(wfRequest);
        applicationProcessingServiceImpl.updateDepartmentToPortalDBs(wfRequest);
    }
}


