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

    private final KafkaProducer kafkaProducer;
    private final ApiStep apiStep;
    private final TransStep transStep;
    private final LeaderR2Repository leaderR2Repository;
    private final CoinR2Repository coinR2Repository;

    @Scheduled(fixedDelay = 30000)
    public void Schedule() throws Exception {
        Flux<Leader> all = leaderR2Repository.findAll();
        all.subscribe(leader -> {
            Mono<Map<String, Coin>> collect = coinR2Repository.findAllByUserId(leader.getUserId())
                    .filter(coin -> coin.getCoinQuantity() > 0.0)
                    .collect(Collectors.toMap(Coin::getCoinName, Function.identity()));
            collect.subscribe(beforeMap -> {
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
                        log.info("1");
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
                                    max.subscribe(maxCoinTranDtos -> {
                                        for (MaxCoinTranDto m:maxCoinTranDtos) {
                                            log.info("======== "+m.toString());
                                        }
                                    });
                                    log.info("2");
                                    return max;
                                });
//                                .map(mono -> {
//                                    mono.subscribe(maxCoinTranDtos -> {
//                                        for (MaxCoinTranDto m:maxCoinTranDtos) {
//                                            log.info("12312312312 "+m.toString());
//                                        }
//                                        Mono<MaxCoinTranDto[]> mono1 = Mono.just(maxCoinTranDtos);
//                                        for(String key:beforeMap.keySet()){
//                                            if(!finalAfterMap1.containsKey(key)){
//                                                List<TransactionDto> transactionDtos = null;
//                                                try {
//                                                    transactionDtos = apiStep.transactionsAPI(leader, finalAfterMap1.get(key).getCoinName());
//                                                } catch (Exception e) {
//                                                    e.printStackTrace();
//                                                }
//                                                mono1 = transStep.transCoinInfo(maxCoinTranDtos, beforeMap.get(key), transactionDtos);
//                                            }
//                                        }
//                                        for (MaxCoinTranDto m:maxCoinTranDtos) {
//                                            log.info("======" + m.toString());
//                                        }

//                                        mono1.subscribe(maxCoinTranDtos1 -> {
//                                            for(int i = 0;i<maxCoinTranDtos1.length;i++){
//                                                if(maxCoinTranDtos1[i].getSearch() != null && maxCoinTranDtos1[i].getSearch().equals("1")){
//                                                    if(sameCoinList.size() > 0){
//                                                        Set<CoinInfoDto> beforeCoinSet = maxCoinTranDtos1[i].getBeforeCoinSet();
//                                                        Set<CoinInfoDto> afterCoinSet = maxCoinTranDtos1[i].getAfterCoinSet();
//                                                        sameCoinList.stream()
//                                                                .forEach(coinInfoDto ->{
//                                                                    beforeCoinSet.add(coinInfoDto);
//                                                                    afterCoinSet.add(coinInfoDto);
//                                                                });
//                                                        maxCoinTranDtos1[i].setBeforeCoinSet(beforeCoinSet);
//                                                        maxCoinTranDtos1[i].setAfterCoinSet(afterCoinSet);
//                                                    }
//                                                    Set<CoinInfoDto> collect = maxCoinTranDtos1[i].getBeforeCoinSet().stream().collect(Collectors.toSet());
//
//                                                    beforeTotal.setCoins(collect);
//                                                    Set<CoinInfoDto> collect1 = maxCoinTranDtos1[i].getAfterCoinSet().stream().collect(Collectors.toSet());
//                                                    afterTotal.setCoins(collect1);
//                                                    buySell.setAfter(afterTotal);
//                                                    buySell.setBefore(beforeTotal);
//                                                    buySell.getBefore().setTotalKRW(maxCoinTranDtos1[i].getBeforeTotalKRW());
//                                                    buySell.getAfter().setTotalKRW(maxCoinTranDtos1[i].getAfterTotalKRW());
//                                                    buySell.setUserId(leader.getUserId());
//                                                    kafkaProducer.sendBuySell(buySell);
//                                                } else if(maxCoinTranDtos1[i].getSearch() != null) {
//                                                    drawlDto.setType(maxCoinTranDtos1[i].getSearch());
//                                                    List<CoinInfoDto> collect = maxCoinTranDtos1[i].getAfterCoinSet().stream().collect(Collectors.toList());
//                                                    drawlDto.setTotalKRW(collect.get(0).getAvgPrice());
//                                                    drawlDto.setUserId(leader.getUserId());
//                                                    kafkaProducer.sendDrawl(drawlDto);
//                                                }
//                                            }
//                                            leader.setLastTransTime(ts);
//                                            log.info(leader.toString());
//                                            leaderR2Repository.save(leader).subscribe();
//                                        });
//                                    });
//                                    return 0;
//                                });
                    });
        });

    }
}