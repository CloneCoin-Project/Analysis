package com.cloneCoin.analysis.service;

import com.cloneCoin.analysis.domain.Leader;
import com.cloneCoin.analysis.domain.User;
import com.cloneCoin.analysis.repository.LeaderR2Repository;
import com.cloneCoin.analysis.repository.LeaderRepository;
import com.cloneCoin.analysis.repository.UserRepository;
import com.netflix.discovery.converters.Auto;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ContextConfiguration
class LeaderServiceTest {

    @Autowired
    UserRepository userRepository;

    @Test
    void test(){
        User user = new User(1L, "nick","name");
        userRepository.save(user).subscribe();
        Mono<User> byId = userRepository.findById(1L);
        byId.subscribe((data) -> {
            System.out.println(data.getFirstname());
        });
    }

}