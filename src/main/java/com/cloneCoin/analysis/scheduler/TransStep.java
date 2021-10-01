package com.cloneCoin.analysis.scheduler;

import com.cloneCoin.analysis.domain.Coin;
import com.cloneCoin.analysis.domain.Leader;
import com.cloneCoin.analysis.dto.CoinInfoDto;
import com.cloneCoin.analysis.dto.MaxCoinTranDto;
import com.cloneCoin.analysis.dto.TransactionDto;
import com.cloneCoin.analysis.repository.CoinRepository;
import com.cloneCoin.analysis.repository.LeaderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;

//@Component
@RequiredArgsConstructor
public class TransStep {

//    private final ApiStep apiStep;
//    private final CoinRepository coinRepository;
//    private final LeaderRepository leaderRepository;
//
//    public void transCoinInfo(MaxCoinTranDto[] maxList, Coin coin, List<TransactionDto> transactionDtos) {
//        Leader leader = coin.getLeader();
//        Double beforeLeaderKRW = leader.getTotalKRW();
//        int count = 0;
//        boolean isTrans = false;
//        CoinInfoDto beforeCoinInfo = coin.toCoinDto();
//
//        for (TransactionDto tran:transactionDtos) {
//            if(tran.getSearch().equals("2")){
//                coin.setCoinQuantity(coin.getCoinQuantity() - tran.getUnits());
//                leader.setTotalKRW(leader.getTotalKRW() + (tran.getUnits() * tran.getPrice()));
//                isTrans = true;
//            }
//            else if(tran.getSearch().equals("1")){
//                Double quantity = coin.getCoinQuantity() + tran.getUnits();
//                Double totalAmount = (coin.getAvgPrice() * coin.getCoinQuantity()) + (tran.getPrice() * tran.getUnits());
//                coin.setCoinQuantity(quantity);
//                coin.setAvgPrice(totalAmount / quantity);
//                isTrans = true;
//            }
//            else {
//                if(isTrans == true){
//                    beforeCoinInfo = saveBS(maxList[count], coin, leader.getTotalKRW(), beforeCoinInfo, beforeLeaderKRW);
//                    beforeLeaderKRW = leader.getTotalKRW();
//                }
//                count++;
//                if(tran.getSearch().equals("4") && maxList[count].getAfterCoinSet().isEmpty()) {
//                    saveDe(maxList[count], leader.getTotalKRW(), tran.getPrice(), tran.getSearch());
//                    leader.setTotalKRW(saveDe(maxList[count], leader.getTotalKRW(), tran.getPrice(), tran.getSearch()));
//                }
//                else if(tran.getSearch().equals("5") && maxList[count].getAfterCoinSet().isEmpty()) {
//                    saveWi(maxList[count], leader.getTotalKRW(), tran.getPrice(), tran.getSearch());
//                    leader.setTotalKRW(saveWi(maxList[count], leader.getTotalKRW(), tran.getPrice(), tran.getSearch()));
//                }
//                count++;
//                isTrans = false;
//            }
//        }
//        if(isTrans == true){
//            saveBS(maxList[count], coin, leader.getTotalKRW(), beforeCoinInfo, beforeLeaderKRW);
//        }
//        coinRepository.save(coin);
//        if(beforeLeaderKRW.compareTo(leader.getTotalKRW()) != 0){
//            leaderRepository.save(leader);
//        }
//    }
//
//    private Double saveWi(MaxCoinTranDto maxCoinTranDto, Double total, Double price, String search){
//        CoinInfoDto krw = CoinInfoDto.builder()
//                .coinName("KRW")
//                .avgPrice(total - price)
//                .coinQuantity(0.0)
//                .build();
//        Set<CoinInfoDto> krwSet = new HashSet<>();
//        krwSet.add(krw);
//        maxCoinTranDto.setAfterCoinSet(krwSet);
//        maxCoinTranDto.setSearch(search);
//        return krw.getAvgPrice();
//    }
//
//    private Double saveDe(MaxCoinTranDto maxCoinTranDto, Double total, Double price, String search){
//        CoinInfoDto krw = CoinInfoDto.builder()
//                .coinName("KRW")
//                .avgPrice(total + price)
//                .coinQuantity(0.0)
//                .build();
//        Set<CoinInfoDto> krwSet = new HashSet<>();
//        krwSet.add(krw);
//        maxCoinTranDto.setAfterCoinSet(krwSet);
//        maxCoinTranDto.setSearch(search);
//        return krw.getAvgPrice();
//    }
//
//    private CoinInfoDto saveBS(MaxCoinTranDto maxCoinTranDto, Coin coin, Double total, CoinInfoDto beforeCoinInfo, Double beforeLeaderKRW){
//        CoinInfoDto coinInfoDto = coin.toCoinDto();
//        Set<CoinInfoDto> afterCoinInfoSet = maxCoinTranDto.getAfterCoinSet();
//        Set<CoinInfoDto> beforeCoinInfoSet = maxCoinTranDto.getBeforeCoinSet();
//        afterCoinInfoSet.add(coinInfoDto);
//        beforeCoinInfoSet.add(beforeCoinInfo);
//
//        maxCoinTranDto.setAfterCoinSet(afterCoinInfoSet);
//        maxCoinTranDto.setBeforeCoinSet(beforeCoinInfoSet);
//        maxCoinTranDto.setSearch("1");
//        maxCoinTranDto.setBeforeTotalKRW(beforeLeaderKRW);
//        maxCoinTranDto.setAfterTotalKRW(total);
//        return coinInfoDto;
//    }
//
//    public int transMaxCount(Leader leader) throws Exception {
//        List<TransactionDto> btc = apiStep.transactionsAPI(leader, "BTC");
//        int count = 0;
//        for (TransactionDto tran:btc) {
//            if(tran.getSearch().equals("4") || tran.getSearch().equals("5")){
//                count++;
//            }
//        }
//        return (count * 2) + 1;
//    }
}
