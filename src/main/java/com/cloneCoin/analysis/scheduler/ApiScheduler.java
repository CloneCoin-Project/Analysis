package com.cloneCoin.analysis.scheduler;

import com.cloneCoin.analysis.domain.Coin;
import com.cloneCoin.analysis.domain.Leader;
import com.cloneCoin.analysis.dto.*;
import com.cloneCoin.analysis.repository.CoinR2Repository;
import com.cloneCoin.analysis.repository.LeaderR2Repository;
import com.cloneCoin.analysis.service.kafka.KafkaProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;


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
                        AtomicReference<Mono<MaxCoinTranDto[]>> result = new AtomicReference<>(Mono.empty());
                        Map<String, Coin> finalAfterMap = afterMap;
                        afterMap.keySet().stream()
                                .forEach(key -> {
                                    if (beforeMap.containsKey(key) && (finalAfterMap.get(key).getCoinQuantity().compareTo(beforeMap.get(key).getCoinQuantity())) == 0) {
                                        sameCoinList.add(beforeMap.get(key).toCoinDto());
                                    } else {
                                        List<TransactionDto> transactionDtos = null;
                                        try {
                                            transactionDtos = apiStep.transactionsAPI(leader, finalAfterMap.get(key).getCoinName());
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        if(beforeMap.containsKey(key)){
                                            result.set(transStep.transCoinInfo(maxList, beforeMap.get(key), transactionDtos));
                                        }else{
                                            log.info("====123 " + beforeMap.get(key));
                                            Coin coinBefore = Coin.builder()
                                                    .coinName(finalAfterMap.get(key).getCoinName())
                                                    .coinQuantity(0.0)
                                                    .leaderId(leader.getUserId())
                                                    .avgPrice(0.0)
                                                    .build();
                                            result.set(transStep.transCoinInfo(maxList, coinBefore, transactionDtos));
                                        }
                                    }
                                });
                        result.get().subscribe(maxCoinTranDtos -> {
                            for (MaxCoinTranDto m:maxCoinTranDtos) {
                                        for(String key:beforeMap.keySet()){
                                            if(!finalAfterMap.containsKey(key)){
                                                List<TransactionDto> transactionDtos = null;
                                                try {
                                                    transactionDtos = apiStep.transactionsAPI(leader, finalAfterMap.get(key).getCoinName());
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                                result.set(transStep.transCoinInfo(maxCoinTranDtos, beforeMap.get(key), transactionDtos));
                                            }
                                        }
                            }
                        });
                        result.get().subscribe(maxCoinTranDtos -> {
                            for(int i = 0;i<maxCoinTranDtos.length;i++){
                                if(maxCoinTranDtos[i].getSearch() != null && maxCoinTranDtos[i].getSearch().equals("1")){
                                    if(sameCoinList.size() > 0){
                                        Set<CoinInfoDto> beforeCoinSet = maxCoinTranDtos[i].getBeforeCoinSet();
                                        Set<CoinInfoDto> afterCoinSet = maxCoinTranDtos[i].getAfterCoinSet();
                                        sameCoinList.stream()
                                                .forEach(coinInfoDto ->{
                                                    beforeCoinSet.add(coinInfoDto);
                                                    afterCoinSet.add(coinInfoDto);
                                                });
                                        maxCoinTranDtos[i].setBeforeCoinSet(beforeCoinSet);
                                        maxCoinTranDtos[i].setAfterCoinSet(afterCoinSet);
                                    }
                                    List<CoinInfoDto> bcollect = maxList[i].getBeforeCoinSet().stream().collect(Collectors.toList());
                                    bcollect.sort(Comparator.comparing(CoinInfoDto::getCoinName));
                                    beforeTotal.setCoins(bcollect);
                                    List<CoinInfoDto> acollect = maxList[i].getAfterCoinSet().stream().collect(Collectors.toList());
                                    acollect.sort(Comparator.comparing(CoinInfoDto::getCoinName));
                                    afterTotal.setCoins(acollect);
                                    buySell.setAfter(afterTotal);
                                    buySell.setBefore(beforeTotal);
                                    buySell.getBefore().setTotalKRW(maxList[i].getBeforeTotalKRW());
                                    buySell.getAfter().setTotalKRW(maxList[i].getAfterTotalKRW());
                                    buySell.setUserId(leader.getUserId());
                                    kafkaProducer.sendBuySell(buySell);
                                } else if(maxCoinTranDtos[i].getSearch() != null) {
                                    drawlDto.setType(maxCoinTranDtos[i].getSearch());
                                    List<CoinInfoDto> collect2 = maxCoinTranDtos[i].getAfterCoinSet().stream().collect(Collectors.toList());
                                    drawlDto.setTotalKRW(collect2.get(0).getAvgPrice());
                                    drawlDto.setUserId(leader.getUserId());
                                    kafkaProducer.sendDrawl(drawlDto);
                                }
                            }
                            leader.setLastTransTime(ts);
                            log.info(leader.toString());
                            leaderR2Repository.save(leader).subscribe();
                        });
                    });
        });

    }
}