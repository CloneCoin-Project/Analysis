package com.cloneCoin.analysis.service.kafka;

import com.cloneCoin.analysis.domain.Leader;
import com.cloneCoin.analysis.dto.DrawlDto;
import com.cloneCoin.analysis.dto.LeaderDto;
import com.cloneCoin.analysis.dto.PushDto;
import com.cloneCoin.analysis.dto.TransactionsDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaProducer {

    private static final String TOPIC = "user-leader-apply-topic";
    private static final String BUYSELL = "buy-sell";
    private static final String DepositWithdrawal = "Deposit-Withdrawal";
    private static final String PUSH = "message-push";
    private static final String SIGNAL = "leader-signal";
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaTemplate<String, String> pushKafkaTemplate;

    public LeaderDto sendTest() {
        LeaderDto leader = new LeaderDto();
        leader.setLeaderId(1L);
        leader.setLeaderName("시헌");
        leader.setApiKey("d6c1e3e6904559b9b98c0ee8f8f69769");
        leader.setSecretKey("c236bbebfea28daf7dba8a1ca4f2dfb0");
        kafkaTemplate.send(TOPIC, leader);
        return leader;
    }

    public void sendBuySell(TransactionsDTO transactionsDTO) {
        System.out.println("SENDING..... " + transactionsDTO);
        kafkaTemplate.send(BUYSELL, transactionsDTO);
    }

    public void sendPush(PushDto pushDto){
        ObjectMapper mapper = new ObjectMapper();
        String s = "";
        try {
            s = mapper.writeValueAsString(pushDto);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        pushKafkaTemplate.send(PUSH, s);
    }

    public void sendSignal(String userId){
        pushKafkaTemplate.send(SIGNAL, userId);
    }

    public void sendDrawl(DrawlDto drawlDto) {
        kafkaTemplate.send(DepositWithdrawal, drawlDto);
    }
}
