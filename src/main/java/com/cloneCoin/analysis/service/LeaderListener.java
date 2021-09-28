package com.cloneCoin.analysis.service;

import com.cloneCoin.analysis.config.CryptUtil;
import com.cloneCoin.analysis.domain.Leader;
import com.cloneCoin.analysis.dto.LeaderDto;
import com.cloneCoin.analysis.repository.LeaderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Slf4j
@Component
@RequiredArgsConstructor
public class LeaderListener {

    @Value("${cryptutil.key}")
    private String key;

    private final LeaderRepository leaderRepository;

    @KafkaListener(topics = "user-kafka", groupId = "foo")
    public void ListenLeader(LeaderDto leader) throws Exception {
        String strToEncrypt = leader.getSecretKey();
        CryptUtil.Aes aes = CryptUtil.getAES();
        String encryptedSecretKey = aes.encrypt(key, strToEncrypt);

        Leader newLeader = Leader.builder()
                .userId(leader.getLeaderId())
                .totalKRW(0.0)
                .lastTransTime(0L)
                .apiKey(leader.getApiKey())
                .secretKey(encryptedSecretKey)
                .build();
        leaderRepository.save(newLeader);

        log.info(String.format("User created -> %s", newLeader));
    }

}
