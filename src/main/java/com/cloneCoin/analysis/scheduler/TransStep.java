package com.cloneCoin.analysis.scheduler;

import com.cloneCoin.analysis.domain.Coin;
import com.cloneCoin.analysis.domain.Leader;
import com.cloneCoin.analysis.domain.Type;
import com.cloneCoin.analysis.dto.CoinInfoDto;
import com.cloneCoin.analysis.dto.MaxCoinTranDto;
import com.cloneCoin.analysis.dto.PushDto;
import com.cloneCoin.analysis.dto.TransactionDto;
import com.cloneCoin.analysis.repository.CoinRepository;
import com.cloneCoin.analysis.repository.LeaderRepository;
import com.cloneCoin.analysis.service.kafka.KafkaProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class TransStep {

    private final ApiStep apiStep;
    private final CoinRepository coinRepository;
    private final LeaderRepository leaderRepository;
    private final KafkaProducer kafkaProducer;

    public void transCoinInfo(MaxCoinTranDto[] maxList, Coin coin, List<TransactionDto> transactionDtos) {
        Leader leader = coin.getLeader();
        Double beforeLeaderKRW = leader.getTotalKRW();
        int count = 0;
        boolean isTrans = false;
        CoinInfoDto beforeCoinInfo = coin.toCoinDto();

        for (TransactionDto tran:transactionDtos) {
            if(tran.getSearch().equals("2")){
                BigDecimal a = new BigDecimal(String.valueOf(coin.getCoinQuantity()));
                BigDecimal b = new BigDecimal(String.valueOf(tran.getUnits()));
                BigDecimal c = new BigDecimal(String.valueOf(tran.getPrice()));
                BigDecimal d = new BigDecimal(String.valueOf(leader.getTotalKRW()));
                coin.setCoinQuantity(Double.parseDouble(String.valueOf(a.subtract(b))));
                if (coin.getCoinQuantity().equals(0.0)) {
                    coin.setAvgPrice(0.0);
                }
                leader.setTotalKRW(Double.parseDouble(String.valueOf(d.add(b.multiply(c)))));
                String msg = leader.getUserName() + "님이 " + coin.getCoinName() + " 코인 " + tran.getUnits() + "개를 " + tran.getPrice() + "원에 판매하였습니다.";
                String type = "SOLD";
                PushDto pushDto = PushDto.builder()
                        .leaderId(leader.getUserId())
                        .type(type)
                        .message(msg)
                        .build();
                kafkaProducer.sendPush(pushDto);
                isTrans = true;
            }
            else if(tran.getSearch().equals("1")){
                BigDecimal a = new BigDecimal(String.valueOf(coin.getCoinQuantity()));
                BigDecimal b = new BigDecimal(String.valueOf(tran.getUnits()));
                BigDecimal c = new BigDecimal(String.valueOf(tran.getPrice()));
                BigDecimal d = new BigDecimal(String.valueOf(coin.getAvgPrice()));
                BigDecimal e = new BigDecimal(String.valueOf(leader.getTotalKRW()));

                Double quantity = Double.parseDouble(String.valueOf(a.add(b)));
                Double totalAmount = Double.parseDouble(String.valueOf((d.multiply(a)).add(c.multiply(b))));
//                leader.setTotalKRW(leader.getTotalKRW() - (tran.getPrice() * tran.getUnits()));
                leader.setTotalKRW(Double.parseDouble(String.valueOf(e.subtract(c.multiply(b)))));
                coin.setCoinQuantity(quantity);
                a = new BigDecimal(String.valueOf(quantity));
                b = new BigDecimal(String.valueOf(totalAmount));
                coin.setAvgPrice(Double.parseDouble(String.valueOf(b.divide(a, MathContext.DECIMAL32))));
                String msg =leader.getUserName() + "님이 " + coin.getCoinName() + " 코인 " + tran.getUnits() + "개를 " + tran.getPrice() + "원에 구매하였습니다.";
                String type = "BOUGHT";
                PushDto pushDto = PushDto.builder()
                        .leaderId(leader.getUserId())
                        .type(type)
                        .message(msg)
                        .build();
                kafkaProducer.sendPush(pushDto);
                isTrans = true;
            }
            else {
                if(isTrans == true){
                    beforeCoinInfo = saveBS(maxList[count], coin, leader.getTotalKRW(), beforeCoinInfo, beforeLeaderKRW);
                    beforeLeaderKRW = leader.getTotalKRW();
                }
                count++;
                if(tran.getSearch().equals("4") && maxList[count].getAfterCoinSet().isEmpty()) {
                    leader.setTotalKRW(saveDe(maxList[count], leader.getTotalKRW(), tran.getPrice(), tran.getSearch()));
                }
                else if(tran.getSearch().equals("5") && maxList[count].getAfterCoinSet().isEmpty()) {
                    leader.setTotalKRW(saveWi(maxList[count], leader.getTotalKRW(), tran.getPrice(), tran.getSearch()));
                }
                count++;
                isTrans = false;
            }
        }
        if(isTrans == true){
            saveBS(maxList[count], coin, leader.getTotalKRW(), beforeCoinInfo, beforeLeaderKRW);
        }
        coinRepository.save(coin);
        if(beforeLeaderKRW.compareTo(leader.getTotalKRW()) != 0){
            leaderRepository.save(leader);
        }
    }

    private Double saveWi(MaxCoinTranDto maxCoinTranDto, Double total, Double price, String search){
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
        maxCoinTranDto.setAfterCoinSet(krwSet);
        maxCoinTranDto.setSearch(search);
        return krw.getAvgPrice();
    }

    private Double saveDe(MaxCoinTranDto maxCoinTranDto, Double total, Double price, String search){
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
