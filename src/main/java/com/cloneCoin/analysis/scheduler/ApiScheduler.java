package com.cloneCoin.analysis.scheduler;

import com.cloneCoin.analysis.domain.Coin;
import com.cloneCoin.analysis.domain.Leader;
import com.cloneCoin.analysis.dto.*;
import com.cloneCoin.analysis.repository.LeaderRepository;
import com.cloneCoin.analysis.service.kafka.KafkaProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
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
                    .filter(coin -> coin.getCoinQuantity()>0.0)
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
                        else {
                            List<TransactionDto> transactionDtos = null;
                            try {
                                transactionDtos = apiStep.transactionsAPI(leader, afterMap.get(key).getCoinName());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            if(beforeMap.containsKey(key)){
                                transStep.transCoinInfo(maxList, beforeMap.get(key), transactionDtos);
                            }else{
                                Coin coinBefore = Coin.builder()
                                        .coinName(afterMap.get(key).getCoinName())
                                        .coinQuantity(0.0)
                                        .leader(leader)
                                        .avgPrice(0.0)
                                        .build();
                                transStep.transCoinInfo(maxList, coinBefore, transactionDtos);
                            }
                        }
                    });

            beforeMap.keySet().stream()
                    .forEach(key -> {
                        if(!afterMap.containsKey(key)){
                            List<TransactionDto> transactionDtos = null;
                            try {
                                transactionDtos = apiStep.transactionsAPI(leader, beforeMap.get(key).getCoinName());
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

                } else if(maxList[i].getSearch() != null && (maxList[i].getSearch().equals("4") || maxList[i].getSearch().equals("5"))) {
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