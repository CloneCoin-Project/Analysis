package com.cloneCoin.analysis.scheduler;

import com.cloneCoin.analysis.domain.Coin;
import com.cloneCoin.analysis.domain.Leader;
import com.cloneCoin.analysis.dto.*;
import com.cloneCoin.analysis.repository.CoinR2Repository;
import com.cloneCoin.analysis.repository.LeaderR2Repository;
import com.cloneCoin.analysis.repository.LeaderRepository;
import com.cloneCoin.analysis.service.kafka.KafkaProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.protocol.types.Field;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Component
@Slf4j
@RequiredArgsConstructor
public class ApiScheduler {

//    private final LeaderRepository leaderRepository;
    private final KafkaProducer kafkaProducer;
    private final ApiStep apiStep;
    private final TransStep transStep;
    private final LeaderR2Repository leaderR2Repository;
    private final CoinR2Repository coinR2Repository;

    @Scheduled(fixedDelay = 30000)
    public void Schedule() throws Exception {
        Flux<Leader> all = leaderR2Repository.findAll();
        all.subscribe(leader -> {
            coinR2Repository.findAllByUserId(leader.getUserId())
                    .filter(coin -> coin.getCoinQuantity() > 0.0)
                    .collect(Collectors.toMap(Coin::getCoinName, Function.identity()))
                    .subscribe(beforeMap -> {
                        Map<String, Coin> afterMap = null;
                        try {
                            afterMap = apiStep.balanceAPI(leader).stream()
                                    .collect(Collectors.toMap(Coin::getCoinName, Function.identity()));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        int maxCount = 0;
                        try {
                            maxCount = transStep.transMaxCount(leader);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        Long ts = System.currentTimeMillis();
                        MaxCoinTranDto[] maxList = new MaxCoinTranDto[maxCount];
                        for(int i = 0;i<maxCount;i++) {
                            maxList[i] = new MaxCoinTranDto();
                        }
                        TotalDto beforeTotal = new TotalDto();
                        TotalDto afterTotal = new TotalDto();
                        TransactionsDTO buySell = new TransactionsDTO();
                        DrawlDto drawlDto = new DrawlDto();
                        List<CoinInfoDto> sameCoinList = new ArrayList<>();
                        Map<String, Coin> finalAfterMap = afterMap;

                        Map<String, Coin> finalAfterMap1 = afterMap;
                        afterMap.keySet().stream()
                                .map(key -> {
                                    Mono<MaxCoinTranDto[]> max = Mono.empty();
                                    if (beforeMap.containsKey(key) && (finalAfterMap.get(key).getCoinQuantity().compareTo(beforeMap.get(key).getCoinQuantity())) == 0) {
                                        sameCoinList.add(beforeMap.get(key).toCoinDto());
                                    } else if (beforeMap.containsKey(key)) {
                                        List<TransactionDto> transactionDtos = null;
                                        try {
                                            transactionDtos = apiStep.transactionsAPI(leader, finalAfterMap.get(key).getCoinName());
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        max = transStep.transCoinInfo(maxList, beforeMap.get(key), transactionDtos);
                                    }
                                    return max;
                                })
                                .forEach(mono -> {
                                    mono.subscribe(maxCoinTranDtos -> {
                                        for (MaxCoinTranDto m:maxCoinTranDtos) {
                                            log.info("====== "+m.toString());
                                        }
                                        Mono<MaxCoinTranDto[]> mono1 = null;
                                        for(String key:beforeMap.keySet()){
                                            if(!finalAfterMap1.containsKey(key)){
                                                log.info("123123123123    "+key);
                                                List<TransactionDto> transactionDtos = null;
                                                try {
                                                    transactionDtos = apiStep.transactionsAPI(leader, finalAfterMap1.get(key).getCoinName());
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                                mono1 = transStep.transCoinInfo(maxCoinTranDtos, beforeMap.get(key), transactionDtos);
                                                mono1.subscribe(maxCoinTranDtos1 -> {
                                                    for (MaxCoinTranDto m:maxCoinTranDtos1) {
                                                        log.info("======123 "+m.toString());
                                                    }
                                                });
                                            }
                                        }
                                    });
                                });
                        leader.setLastTransTime(ts);
                        log.info(leader.toString());
                        leaderR2Repository.save(leader).subscribe();
                    });
        });

    }
}