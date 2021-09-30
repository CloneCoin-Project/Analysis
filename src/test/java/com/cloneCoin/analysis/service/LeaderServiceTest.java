package com.cloneCoin.analysis.service;

import com.cloneCoin.analysis.domain.Leader;
import com.cloneCoin.analysis.repository.LeaderRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class LeaderServiceTest {

    @Autowired
    LeaderRepository leaderRepository;

    @Test
    void LeaderRepoTest() {
        Leader newLeader = Leader.builder()
                .apiKey("12312412")
                .secretKey("12123123")
                .lastTransTime(1L)
                .totalKRW(1.2)
                .userId(1L)
                .build();
        //create
        leaderRepository.save(newLeader);
        //find
        Leader leader = leaderRepository.findById(1L).orElseThrow();
        Assertions.assertThat(newLeader.getApiKey().equals(leader.getApiKey()));

    }
}