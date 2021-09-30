package com.cloneCoin.analysis.scheduler;

import com.cloneCoin.analysis.domain.Coin;
import com.cloneCoin.analysis.domain.Leader;
import com.cloneCoin.analysis.dto.*;
import com.cloneCoin.analysis.repository.LeaderRepository;
import com.cloneCoin.analysis.service.kafka.KafkaProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ApiScheduler {

    private final LeaderRepository leaderRepository;
    private final KafkaProducer kafkaProducer;
    private final ApiStep apiStep;
    private final TransStep transStep;

    @Scheduled(fixedDelay = 30000)
    public void Schedule() throws Exception {
        List<Leader> leaders = leaderRepository.findAll();

        for (Leader leader:leaders) {
            Map<String, Coin> beforeMap = leader.getCoinList().stream()
                    .collect(Collectors.toMap(Coin::getCoinName, Function.identity()));
            Map<String, Coin> afterMap = apiStep.balanceAPI(leader).stream()
                    .collect(Collectors.toMap(Coin::getCoinName, Function.identity()));
            int maxCount = transStep.transMaxCount(leader);
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

            afterMap.keySet().stream()
                    .forEach(key -> {
                        if(beforeMap.containsKey(key) && (afterMap.get(key).getCoinQuantity().compareTo(beforeMap.get(key).getCoinQuantity()))== 0){
                            sameCoinList.add(beforeMap.get(key).toCoinDto());
                        }
                        else if(beforeMap.containsKey(key)){
                            List<TransactionDto> transactionDtos = null;
                            try {
                                transactionDtos = apiStep.transactionsAPI(leader, afterMap.get(key).getCoinName());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            transStep.transCoinInfo(maxList, beforeMap.get(key), transactionDtos);
                        }
                    });

            beforeMap.keySet().stream()
                    .forEach(key -> {
                        if(!afterMap.containsKey(key)){
                            List<TransactionDto> transactionDtos = null;
                            try {
                                transactionDtos = apiStep.transactionsAPI(leader, afterMap.get(key).getCoinName());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            transStep.transCoinInfo(maxList, beforeMap.get(key), transactionDtos);
                        }
                    });

            for(int i = 0;i<maxList.length;i++){
                if(maxList[i].getSearch() != null && maxList[i].getSearch().equals("1")){
                    if(sameCoinList.size() > 0){
                        Set<CoinInfoDto> beforeCoinSet = maxList[i].getBeforeCoinSet();
                        Set<CoinInfoDto> afterCoinSet = maxList[i].getAfterCoinSet();
                        sameCoinList.stream()
                                .forEach(coinInfoDto ->{
                                    beforeCoinSet.add(coinInfoDto);
                                    afterCoinSet.add(coinInfoDto);
                                        });
                        maxList[i].setBeforeCoinSet(beforeCoinSet);
                        maxList[i].setAfterCoinSet(afterCoinSet);
                    }
                    Set<CoinInfoDto> collect = maxList[i].getBeforeCoinSet().stream().collect(Collectors.toSet());
                    beforeTotal.setCoins(collect);
                    Set<CoinInfoDto> collect1 = maxList[i].getAfterCoinSet().stream().collect(Collectors.toSet());
                    afterTotal.setCoins(collect1);
                    buySell.setAfter(afterTotal);
                    buySell.setBefore(beforeTotal);
                    buySell.getBefore().setTotalKRW(maxList[i].getBeforeTotalKRW());
                    buySell.getAfter().setTotalKRW(maxList[i].getAfterTotalKRW());
                    buySell.setUserId(leader.getUserId());
                    kafkaProducer.sendBuySell(buySell);

                } else if(maxList[i].getSearch() != null) {
                    drawlDto.setType(maxList[i].getSearch());
                    List<CoinInfoDto> collect = maxList[i].getAfterCoinSet().stream().collect(Collectors.toList());
                    drawlDto.setTotalKRW(collect.get(0).getAvgPrice());
                    drawlDto.setUserId(leader.getUserId());
                    kafkaProducer.sendDrawl(drawlDto);
                }
            }
            leader.setLastTransTime(ts);
            leaderRepository.save(leader);
        }
    }
}