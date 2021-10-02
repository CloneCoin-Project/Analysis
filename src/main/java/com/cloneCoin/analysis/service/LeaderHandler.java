package com.cloneCoin.analysis.service;

import com.cloneCoin.analysis.domain.Coin;
import com.cloneCoin.analysis.dto.LeaderDto;
import com.cloneCoin.analysis.dto.LeadersDto;
import com.cloneCoin.analysis.repository.CoinR2Repository;
import com.cloneCoin.analysis.service.kafka.KafkaProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
@Slf4j
public class LeaderHandler {

    private final KafkaProducer kafkaProducer;
    private final CoinR2Repository coinR2Repository;

    public Mono<ServerResponse> findAllLeadersCoins() {
        Mono<List<Coin>> listMono = coinR2Repository.findAll().collectList();
        Mono<Stream<LeadersDto>> map = listMono.map(coins -> {
            Stream<Long> longStream = coins.stream().map(coin -> coin.getLeaderId());
            return longStream.distinct()
                    .map(leaderId -> new LeadersDto(leaderId, coins.stream()
                            .filter(coin -> coin.getLeaderId()==leaderId)
                            .filter(coin -> coin.getCoinQuantity() > 0.0)
                            .map(coin -> coin.toCoinDto()).collect(Collectors.toList())));
        });
        return ServerResponse.ok().body(map, Stream.class);
    }

    //TEST
    public Mono<ServerResponse> kafka() {
        LeaderDto leaderDto = kafkaProducer.sendTest();
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(leaderDto);
    }
}
