package com.cloneCoin.analysis.service;

import com.cloneCoin.analysis.domain.Coin;
import com.cloneCoin.analysis.domain.Leader;
import com.cloneCoin.analysis.domain.User;
import com.cloneCoin.analysis.dto.CoinInfoDto;
import com.cloneCoin.analysis.dto.LeaderDto;
import com.cloneCoin.analysis.dto.LeadersDto;
import com.cloneCoin.analysis.repository.LeaderR2Repository;
import com.cloneCoin.analysis.repository.LeaderRepository;
import com.cloneCoin.analysis.repository.UserRepository;
import com.cloneCoin.analysis.service.kafka.KafkaProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class LeaderHandler {

    private final KafkaProducer kafkaProducer;
//    private final LeaderRepository leaderRepository;
    private final LeaderR2Repository leaderR2Repository;
    private final UserRepository userRepository;


    public Mono<ServerResponse> saveOne(){
        Leader build = Leader.builder()
                .userId(1L)
                .totalKRW(1.1)
                .lastTransTime(121L)
                .apiKey("123")
                .secretKey("123")
                .build();
        leaderR2Repository.save(build).subscribe();

        return ServerResponse.ok().body(Mono.just(build), Leader.class);
    }

    public Mono<ServerResponse> findAllLeadersCoins() {
        List<User> leaders = new ArrayList<>();
        Flux<Leader> all = leaderR2Repository.findAll();
        return ServerResponse.ok().body(all, Leader.class);
    }

    //TEST
    public Mono<ServerResponse> kafka() {
        LeaderDto leaderDto = kafkaProducer.sendTest();
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(leaderDto);
    }
}
