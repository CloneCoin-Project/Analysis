package com.cloneCoin.analysis.service;

import com.cloneCoin.analysis.domain.Coin;
import com.cloneCoin.analysis.domain.Leader;
import com.cloneCoin.analysis.dto.CoinInfoDto;
import com.cloneCoin.analysis.dto.LeaderDto;
import com.cloneCoin.analysis.dto.LeadersDto;
import com.cloneCoin.analysis.repository.LeaderR2Repository;
import com.cloneCoin.analysis.repository.LeaderRepository;
import com.cloneCoin.analysis.service.kafka.KafkaProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class LeaderHandler {

    private final KafkaProducer kafkaProducer;
//    private final LeaderRepository leaderRepository;
    private final LeaderR2Repository leaderR2Repository;

    public Mono<ServerResponse> findAllLeadersCoins() {
        List<LeadersDto> leaders = new ArrayList<>();
        Flux<Leader> all = leaderR2Repository.findAll();
        all.doOnNext(leader -> {
            LeadersDto leadersDto = new LeadersDto();
            List<Coin> coinList = leader.getCoinList();
            List<CoinInfoDto> collect = coinList.stream()
                    .map(coin -> coin.toCoinDto())
                    .collect(Collectors.toList());
            leadersDto.setLeaderId(leader.getUserId());
            leadersDto.setCoins(collect);
            leaders.add(leadersDto);
        });
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(leaders);
    }

    //TEST
    public Mono<ServerResponse> kafka() {
        LeaderDto leaderDto = kafkaProducer.sendTest();
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(leaderDto);
    }
}
