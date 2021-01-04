package com.igot.workflow.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.igot.workflow.models.WfRequest;
import com.igot.workflow.service.impl.NotificationServiceImpl;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.stereotype.Service;

@Service
public class NotificationConsumer {
    Logger logger = LogManager.getLogger(ApplicationProcessingConsumer.class);

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private NotificationServiceImpl notificationService;

    @KafkaListener(id = "id2", groupId = "workflowContentTopic-consumer", topicPartitions = {@TopicPartition(topic = "${kafka.topics.workflow.request}", partitions = {"0", "1", "2", "3"})})
    public void processMessage(ConsumerRecord<String, String> data) {
        WfRequest wfRequest = null;
        try {
            String message = String.valueOf(data.value());
            wfRequest = mapper.readValue(message, WfRequest.class);
            logger.info("Recevied data in notification consumer : {}", mapper.writeValueAsString(wfRequest));
        } catch (Exception ex) {
            logger.error("Error while deserialization the object value", ex);
        }
        notificationService.sendNotification(wfRequest);
    }
}
