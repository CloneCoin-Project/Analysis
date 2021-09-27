package com.cloneCoin.analysis.service;

import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaProducer {

    private static final String TOPIC = "kafka-chat";
    private final KafkaTemplate<String, String> kafkaTemplate;

    public void sendTest() {
        String test = "TestMessage";
        kafkaTemplate.send(TOPIC, test);
    }
}
