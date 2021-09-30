package com.cloneCoin.analysis.service.kafka;

import com.cloneCoin.analysis.dto.DrawlDto;
import com.cloneCoin.analysis.dto.LeaderDto;
import com.cloneCoin.analysis.dto.TransactionsDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaProducer {

    private static final String TOPIC = "user-kafka";
    private static final String BUYSELL = "buy-sell";
    private static final String DepositWithdrawal = "Deposit-Withdrawal";
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendTest() {
        LeaderDto leader = new LeaderDto();
        leader.setLeaderId(1L);
        leader.setApiKey("25df4195dd072124e06545629a515d56");
        leader.setSecretKey("058f77c9fb075ee1b2077e825e77291c");
        kafkaTemplate.send(TOPIC, leader);
    }

    public void sendBuySell(TransactionsDTO transactionsDTO) {
        System.out.println("SENDING..... " + transactionsDTO);
        kafkaTemplate.send(BUYSELL, transactionsDTO);
    }

    public void sendDrawl(DrawlDto drawlDto) {
        kafkaTemplate.send(DepositWithdrawal, drawlDto);
    }
}
