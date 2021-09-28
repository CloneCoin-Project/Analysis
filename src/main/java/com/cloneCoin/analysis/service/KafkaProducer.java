package com.cloneCoin.analysis.service;

import com.cloneCoin.analysis.dto.LeaderDto;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaProducer {

    private static final String TOPIC = "user-kafka";
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendTest() {
        LeaderDto leader = new LeaderDto();
        leader.setLeaderId(1L);
        leader.setApiKey("25df4195dd072124e06545629a515d56");
        leader.setSecretKey("058f77c9fb075ee1b2077e825e77291c");
        kafkaTemplate.send(TOPIC, leader);
    }
}
