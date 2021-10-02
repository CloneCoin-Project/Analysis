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

                        afterMap.keySet().stream()
                                .map(key -> {
                                    Mono<MaxCoinTranDto[]> map = Mono.empty();
                                    if (beforeMap.containsKey(key) && (finalAfterMap.get(key).getCoinQuantity().compareTo(beforeMap.get(key).getCoinQuantity())) == 0) {
                                        sameCoinList.add(beforeMap.get(key).toCoinDto());
                                    } else if (beforeMap.containsKey(key)) {
                                        List<TransactionDto> transactionDtos = null;
                                        try {
                                            transactionDtos = apiStep.transactionsAPI(leader, finalAfterMap.get(key).getCoinName());
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        map = transStep.transCoinInfo(maxList, beforeMap.get(key), transactionDtos);
                                    }
                                    return map;
                                })
                                .forEach(mono -> {
                                    mono.subscribe(maxCoinTranDtos -> {
                                        Stream<Mono<MaxCoinTranDto[]>> monoStream1 = beforeMap.keySet().stream()
                                                .map(k -> {
                                                    Mono<MaxCoinTranDto[]> map = Mono.empty();
                                                    if(!finalAfterMap.containsKey(k)){
                                                        List<TransactionDto> transactionDtos = null;
                                                        try {
                                                            transactionDtos = apiStep.transactionsAPI(leader, finalAfterMap.get(k).getCoinName());
                                                        } catch (Exception e) {
                                                            e.printStackTrace();
                                                        }
                                                        map = transStep.transCoinInfo(maxCoinTranDtos, beforeMap.get(k), transactionDtos);
                                                    }
                                                    return map;
                                                })
                                                .filter(mono1 -> !mono.equals(Mono.empty()));
//                                        monoStream1.forEach(mono1 -> {
//                                            mono1.subscribe(maxCoinTranDtos1 -> {
//                                                log.info("123123"+maxCoinTranDtos1[0].toString());
//                                                log.info("123123"+maxCoinTranDtos1[1].getAfterCoinSet().toArray().toString());
//                                                log.info("123123"+maxCoinTranDtos1[2].toString());
//                                            });
//                                        });
                                        monoStream1.forEach(mono1 -> {
                                            mono1.subscribe(maxCoinTranDtos1 -> {
                                                Arrays.stream(maxCoinTranDtos1).forEach(maxCoinTranDto -> {
                                                    log.info("==========="+maxCoinTranDto.toString());
                                                    if(maxCoinTranDto.getSearch() != null && maxCoinTranDto.getSearch().equals("1")){
                                                        if(sameCoinList.size() > 0){
                                                            Set<CoinInfoDto> beforeCoinSet = maxCoinTranDto.getBeforeCoinSet();
                                                            Set<CoinInfoDto> afterCoinSet = maxCoinTranDto.getAfterCoinSet();
                                                            sameCoinList.stream()
                                                                    .forEach(coinInfoDto ->{
                                                                        beforeCoinSet.add(coinInfoDto);
                                                                        afterCoinSet.add(coinInfoDto);
                                                                    });
                                                            maxCoinTranDto.setBeforeCoinSet(beforeCoinSet);
                                                            maxCoinTranDto.setAfterCoinSet(afterCoinSet);
                                                        }
                                                        beforeTotal.setCoins(maxCoinTranDto.getBeforeCoinSet().stream().collect(Collectors.toSet()));
                                                        afterTotal.setCoins(maxCoinTranDto.getAfterCoinSet().stream().collect(Collectors.toSet()));
                                                        buySell.setAfter(afterTotal);
                                                        buySell.setBefore(beforeTotal);
                                                        buySell.getBefore().setTotalKRW(maxCoinTranDto.getBeforeTotalKRW());
                                                        buySell.getAfter().setTotalKRW(maxCoinTranDto.getAfterTotalKRW());
                                                        buySell.setUserId(leader.getUserId());
                                                        log.info("================== SENDBUYSEL");
                                                        kafkaProducer.sendBuySell(buySell);

                                                    } else if(maxCoinTranDto.getSearch() != null && (maxCoinTranDto.getSearch().equals("4") || maxCoinTranDto.getSearch().equals("5"))) {
                                                        drawlDto.setType(maxCoinTranDto.getSearch());
                                                        List<CoinInfoDto> collect = maxCoinTranDto.getAfterCoinSet().stream().collect(Collectors.toList());
                                                        drawlDto.setTotalKRW(collect.get(0).getAvgPrice());
                                                        drawlDto.setUserId(leader.getUserId());
                                                        log.info("================= SENDDRAWL");
                                                        kafkaProducer.sendDrawl(drawlDto);
                                                    }
                                                });
                                            });
                                        });
                                    });
                                });
                        leader.setLastTransTime(ts);
                        log.info(leader.toString());
                        leaderR2Repository.save(leader).subscribe();
                    });
        });

    }
}