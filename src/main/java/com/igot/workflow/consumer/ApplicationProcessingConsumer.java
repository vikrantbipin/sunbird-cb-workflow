package com.igot.workflow.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.igot.workflow.models.WfRequest;
import com.igot.workflow.service.impl.ApplicationProcessingService;
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
    private ApplicationProcessingService applicationProcessingService;


    @KafkaListener(id = "id0", groupId = "workflowContentTopic-consumer", topicPartitions = {@TopicPartition(topic = "${kafka.topics.workflow.request}", partitions = {"0", "1", "2", "3"})})
    public void processMessage(ConsumerRecord<String, String> data) {
        String message = String.valueOf(data.value());
        WfRequest wfRequest = null;
        try {
            wfRequest = mapper.readValue(message, WfRequest.class);
            applicationProcessingService.processWfApplicationRequest(wfRequest);
        } catch (Exception ex) {
            logger.error("Error while deserialization the object value", ex);
        }
    }
}


