package com.cloneCoin.analysis.scheduler;

import com.cloneCoin.analysis.domain.Coin;
import com.cloneCoin.analysis.domain.Leader;
import com.cloneCoin.analysis.dto.CoinInfoDto;
import com.cloneCoin.analysis.dto.MaxCoinTranDto;
import com.cloneCoin.analysis.dto.TransactionDto;
import com.cloneCoin.analysis.repository.CoinR2Repository;
import com.cloneCoin.analysis.repository.LeaderR2Repository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceArray;

@Component
@Slf4j
@RequiredArgsConstructor
public class TransStep {

    private final ApiStep apiStep;
    private final LeaderR2Repository leaderR2Repository;
    private final CoinR2Repository coinR2Repository;

    public AtomicReferenceArray<MaxCoinTranDto> transCoinInfo(AtomicReferenceArray<MaxCoinTranDto> maxList, Coin coin, List<TransactionDto> transactionDtos) {
        leaderR2Repository.findByUserId(coin.getLeaderId()).subscribe(leader -> {
            Double beforeLeaderKRW = leader.getTotalKRW();
            int count = 0;
            boolean isTrans = false;
            CoinInfoDto beforeCoinInfo = coin.toCoinDto();

            for (TransactionDto tran:transactionDtos) {
                if(tran.getSearch().equals("2")){
                    coin.setCoinQuantity(coin.getCoinQuantity() - tran.getUnits());
                    if (coin.getCoinQuantity().equals(0.0)) {
                        coin.setAvgPrice(0.0);
                    }
                    leader.setTotalKRW(leader.getTotalKRW() + (tran.getUnits() * tran.getPrice()));
                    isTrans = true;
                }
                else if(tran.getSearch().equals("1")){
                    Double quantity = coin.getCoinQuantity() + tran.getUnits();
                    Double totalAmount = (coin.getAvgPrice() * coin.getCoinQuantity()) + (tran.getPrice() * tran.getUnits());
                    leader.setTotalKRW(leader.getTotalKRW() - (tran.getPrice() * tran.getUnits()));
                    coin.setCoinQuantity(quantity);
                    coin.setAvgPrice(totalAmount / quantity);
                    isTrans = true;
                }
                else {
                    if(isTrans == true){
                        beforeCoinInfo = saveBS(maxList, count, coin, leader.getTotalKRW(), beforeCoinInfo, beforeLeaderKRW);
                        beforeLeaderKRW = leader.getTotalKRW();
                    }
                    count++;
                    if(tran.getSearch().equals("4") && maxList.get(count).getAfterCoinSet().isEmpty()) {
                        leader.setTotalKRW(saveDe(maxList, count, leader.getTotalKRW(), tran.getPrice(), tran.getSearch()));
                    }
                    else if(tran.getSearch().equals("5") && maxList.get(count).getAfterCoinSet().isEmpty()) {
                        leader.setTotalKRW(saveWi(maxList, count, leader.getTotalKRW(), tran.getPrice(), tran.getSearch()));
                    }
                    count++;
                    isTrans = false;
                }
            }
            if(isTrans == true){
                saveBS(maxList, count, coin, leader.getTotalKRW(), beforeCoinInfo, beforeLeaderKRW);
            }
            coinR2Repository.save(coin).subscribe();
            if(beforeLeaderKRW.compareTo(leader.getTotalKRW()) != 0){
                leaderR2Repository.save(leader).subscribe();
            }
        });

        return maxList;
    }

    private Double saveWi(AtomicReferenceArray<MaxCoinTranDto> maxList, int i, Double total, Double price, String search){
        BigDecimal a = new BigDecimal(Double.parseDouble(String.valueOf(total)));
        BigDecimal b = new BigDecimal(Double.parseDouble(String.valueOf(price)));
        Double result = Double.parseDouble(String.valueOf(a.subtract(b)));
        CoinInfoDto krw = CoinInfoDto.builder()
                .coinName("KRW")
                .avgPrice(result)
                .coinQuantity(0.0)
                .build();
        Set<CoinInfoDto> krwSet = new HashSet<>();
        krwSet.add(krw);
        maxList.get(i).setAfterCoinSet(krwSet);
        maxList.get(i).setSearch(search);
        return krw.getAvgPrice();
    }

    private Double saveDe(AtomicReferenceArray<MaxCoinTranDto> maxList, int i, Double total, Double price, String search){
        BigDecimal a = new BigDecimal(Double.parseDouble(String.valueOf(total)));
        BigDecimal b = new BigDecimal(Double.parseDouble(String.valueOf(price)));
        Double result = Double.parseDouble(String.valueOf(a.add(b)));
        CoinInfoDto krw = CoinInfoDto.builder()
                .coinName("KRW")
                .avgPrice(result)
                .coinQuantity(0.0)
                .build();
        Set<CoinInfoDto> krwSet = new HashSet<>();
        krwSet.add(krw);
        maxList.get(i).setAfterCoinSet(krwSet);
        maxList.get(i).setSearch(search);
        return krw.getAvgPrice();
    }

    private CoinInfoDto saveBS(AtomicReferenceArray<MaxCoinTranDto> maxList, int i, Coin coin, Double total, CoinInfoDto beforeCoinInfo, Double beforeLeaderKRW){
        CoinInfoDto coinInfoDto = coin.toCoinDto();
        Set<CoinInfoDto> afterCoinInfoSet = maxList.get(i).getAfterCoinSet();
        Set<CoinInfoDto> beforeCoinInfoSet = maxList.get(i).getBeforeCoinSet();
        afterCoinInfoSet.add(coinInfoDto);
        beforeCoinInfoSet.add(beforeCoinInfo);

        maxList.get(i).setAfterCoinSet(afterCoinInfoSet);
        maxList.get(i).setBeforeCoinSet(beforeCoinInfoSet);
        maxList.get(i).setSearch("1");
        maxList.get(i).setBeforeTotalKRW(beforeLeaderKRW);
        maxList.get(i).setAfterTotalKRW(total);
        return coinInfoDto;
    }

    public int transMaxCount(Leader leader) throws Exception {
        List<TransactionDto> btc = apiStep.transactionsAPI(leader, "BTC");
        int count = 0;
        for (TransactionDto tran:btc) {
            if(tran.getSearch().equals("4") || tran.getSearch().equals("5")){
                count++;
            }
        }
        return (count * 2) + 1;
    }
}
