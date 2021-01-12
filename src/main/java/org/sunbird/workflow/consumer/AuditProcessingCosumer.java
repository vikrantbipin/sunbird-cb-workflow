package org.sunbird.workflow.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.stereotype.Service;
import org.sunbird.workflow.models.WfRequest;
import org.sunbird.workflow.service.impl.WorkflowAuditProcessingServiceImpl;

@Service
public class AuditProcessingCosumer {

  //  Logger logger = LogManager.getLogger(AuditProcessingCosumer.class);

//    @Autowired
//    private ObjectMapper mapper;
//
//    @Autowired
//    private WorkflowAuditProcessingServiceImpl workflowAuditProcessingService;
//
//
//    @KafkaListener(id = "id1", groupId = "workflowContentTopic1-consumer", topicPartitions = {@TopicPartition(topic = "${kafka.topics.workflow.request}", partitions = {"0", "1", "2", "3"})})
//    public void processMessage(ConsumerRecord<String, String> data) {
//        try {
//            String message = String.valueOf(data.value());
//            WfRequest wfRequest = null;
//            wfRequest = mapper.readValue(message, WfRequest.class);
//            logger.info("Recevied data in audit consumer : {}", mapper.writeValueAsString(wfRequest));
//            workflowAuditProcessingService.createAudit(wfRequest);
//        } catch (Exception ex) {
//            logger.error("Error while deserialization the object value", ex);
//        }
//    }
}
