package com.cloneCoin.analysis.scheduler;

import com.cloneCoin.analysis.domain.Coin;
import com.cloneCoin.analysis.domain.Leader;
import com.cloneCoin.analysis.dto.CoinInfoDto;
import com.cloneCoin.analysis.dto.MaxCoinTranDto;
import com.cloneCoin.analysis.dto.TransactionDto;
import com.cloneCoin.analysis.repository.CoinR2Repository;
import com.cloneCoin.analysis.repository.CoinRepository;
import com.cloneCoin.analysis.repository.LeaderR2Repository;
import com.cloneCoin.analysis.repository.LeaderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.validation.constraints.Max;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class TransStep {

    private final ApiStep apiStep;
    private final LeaderR2Repository leaderR2Repository;
    private final CoinR2Repository coinR2Repository;

    public Mono<MaxCoinTranDto[]> transCoinInfo(MaxCoinTranDto[] maxList, Coin coin, List<TransactionDto> transactionDtos) {
        MaxCoinTranDto[] max = new MaxCoinTranDto[maxList.length];
        Mono<MaxCoinTranDto[]> map = leaderR2Repository.findByUserId(coin.getLeaderId())
                .map(leader -> {
                    Double beforeLeaderKRW = leader.getTotalKRW();
                    int count = 0;
                    boolean isTrans = false;
                    CoinInfoDto beforeCoinInfo = coin.toCoinDto();
                    for (TransactionDto tran : transactionDtos) {
                        if (tran.getSearch().equals("2")) {
                            coin.setCoinQuantity(coin.getCoinQuantity() - tran.getUnits());
                            leader.setTotalKRW(leader.getTotalKRW() + (tran.getUnits() * tran.getPrice()));
                            isTrans = true;
                        } else if (tran.getSearch().equals("1")) {
                            Double quantity = coin.getCoinQuantity() + tran.getUnits();
                            Double totalAmount = (coin.getAvgPrice() * coin.getCoinQuantity()) + (tran.getPrice() * tran.getUnits());
                            coin.setCoinQuantity(quantity);
                            coin.setAvgPrice(totalAmount / quantity);
                            isTrans = true;
                        } else {
                            if (isTrans == true) {
                                beforeCoinInfo = saveBS(maxList[count], coin, leader.getTotalKRW(), beforeCoinInfo, beforeLeaderKRW);
                                beforeLeaderKRW = leader.getTotalKRW();
                            }
                            count++;
                            if (tran.getSearch().equals("4") && maxList[count].getAfterCoinSet().isEmpty()) {
                                leader.setTotalKRW(saveDe(maxList[count], leader.getTotalKRW(), tran.getPrice(), tran.getSearch()));
                            } else if (tran.getSearch().equals("5") && maxList[count].getAfterCoinSet().isEmpty()) {
                                leader.setTotalKRW(saveWi(maxList[count], leader.getTotalKRW(), tran.getPrice(), tran.getSearch()));
                            }
                            count++;
                            isTrans = false;
                        }
                    }
                    if (isTrans == true) {
                        saveBS(maxList[count], coin, leader.getTotalKRW(), beforeCoinInfo, beforeLeaderKRW);
                    }
                    coinR2Repository.save(coin).subscribe();
                    if (beforeLeaderKRW.compareTo(leader.getTotalKRW()) != 0) {
                        leaderR2Repository.save(leader).subscribe();
                    }
//                    log.info("12312312313"+maxList[0].toString());
//                    log.info("12312312313"+maxList[1].toString());
//                    log.info("12312312313"+maxList[2].toString());
                    return maxList;
                });
        return map;
    }

    private Double saveWi(MaxCoinTranDto maxCoinTranDto, Double total, Double price, String search){
        CoinInfoDto krw = CoinInfoDto.builder()
                .coinName("KRW")
                .avgPrice(total - price)
                .coinQuantity(0.0)
                .build();
        Set<CoinInfoDto> krwSet = new HashSet<>();
        krwSet.add(krw);
        maxCoinTranDto.setAfterCoinSet(krwSet);
        maxCoinTranDto.setSearch(search);
        return krw.getAvgPrice();
    }

    private Double saveDe(MaxCoinTranDto maxCoinTranDto, Double total, Double price, String search){
        CoinInfoDto krw = CoinInfoDto.builder()
                .coinName("KRW")
                .avgPrice(total + price)
                .coinQuantity(0.0)
                .build();
        Set<CoinInfoDto> krwSet = new HashSet<>();
        krwSet.add(krw);
        maxCoinTranDto.setAfterCoinSet(krwSet);
        maxCoinTranDto.setSearch(search);
        return krw.getAvgPrice();
    }

    private CoinInfoDto saveBS(MaxCoinTranDto maxCoinTranDto, Coin coin, Double total, CoinInfoDto beforeCoinInfo, Double beforeLeaderKRW){
        CoinInfoDto coinInfoDto = coin.toCoinDto();
        Set<CoinInfoDto> afterCoinInfoSet = maxCoinTranDto.getAfterCoinSet();
        Set<CoinInfoDto> beforeCoinInfoSet = maxCoinTranDto.getBeforeCoinSet();
        afterCoinInfoSet.add(coinInfoDto);
        beforeCoinInfoSet.add(beforeCoinInfo);

        maxCoinTranDto.setAfterCoinSet(afterCoinInfoSet);
        maxCoinTranDto.setBeforeCoinSet(beforeCoinInfoSet);
        maxCoinTranDto.setSearch("1");
        maxCoinTranDto.setBeforeTotalKRW(beforeLeaderKRW);
        maxCoinTranDto.setAfterTotalKRW(total);
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
