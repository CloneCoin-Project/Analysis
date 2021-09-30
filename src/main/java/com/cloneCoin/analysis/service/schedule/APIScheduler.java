package com.cloneCoin.analysis.service.schedule;

import com.cloneCoin.analysis.config.CryptUtil;
import com.cloneCoin.analysis.domain.Coin;
import com.cloneCoin.analysis.domain.Leader;
import com.cloneCoin.analysis.dto.*;
import com.cloneCoin.analysis.repository.CoinRepository;
import com.cloneCoin.analysis.repository.LeaderRepository;
import com.cloneCoin.analysis.service.Api_Client;
import com.cloneCoin.analysis.service.KafkaProducer;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.SQLOutput;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class APIScheduler {

    @Value("${cryptutil.key}")
    private String key;

    private final LeaderRepository leaderRepository;
    private final CoinRepository coinRepository;
    private final KafkaProducer kafkaProducer;

    @Scheduled(fixedDelay = 30000)
    public void Schedule() throws Exception {
        List<Leader> leaders = leaderRepository.findAll();

        for (Leader leader:leaders) {
            List<Coin> beforeCoinList = leader.getCoinList();
            List<Coin> afterCoinList = balanceAPI(leader);
            Map<String, Coin> beforeMap = beforeCoinList.stream()
                    .collect(Collectors.toMap(Coin::getCoinName, Function.identity()));
            Map<String, Coin> afterMap = afterCoinList.stream()
                    .collect(Collectors.toMap(Coin::getCoinName, Function.identity()));
            int maxCount = transMaxCount(leader);
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
            System.out.println("======= BEFORE LIST =======");
            for (Coin coins:beforeCoinList) {
                System.out.println("CoinName : " + coins.getCoinName() + ", CoinQuantity : " + coins.getCoinQuantity() + ", AvgPrice : " + coins.getAvgPrice());
            }
            System.out.println("======= AFTER LIST ========");
            for (Coin coins:afterCoinList) {
                System.out.println("CoinName : " + coins.getCoinName() + ", CoinQuantity : " + coins.getCoinQuantity() + ", AvgPrice : " + coins.getAvgPrice());
            }

            for (String key:afterMap.keySet()) {
                if(beforeMap.containsKey(key) && (afterMap.get(key).getCoinQuantity().compareTo(beforeMap.get(key).getCoinQuantity()))== 0){
                    sameCoinList.add(beforeMap.get(key).toCoinDto());
                }
                else if(beforeMap.containsKey(key)){
                    List<TransactionDto> transactionDtos = transactionsAPI(leader, afterMap.get(key).getCoinName());
                    transCoinInfo(maxList, beforeMap.get(key), transactionDtos);
                }
            }

            for(String key:beforeMap.keySet()){
                if(!afterMap.containsKey(key)){
                    List<TransactionDto> transactionDtos = transactionsAPI(leader, afterMap.get(key).getCoinName());
                    transCoinInfo(maxList, beforeMap.get(key), transactionDtos);
                }
            }
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
//                    System.out.println("Search = " + maxList[i].getSearch() + ", count = " + i);
//                    System.out.println("============ BEFORE ============= ");
//                    for (CoinInfoDto coins : maxList[i].getBeforeCoinSet()) {
//                        System.out.println("CoinName : " + coins.getCoinName() + ", CoinQuantity : " + coins.getCoinQuantity() + ", AvgPrice : " + coins.getAvgPrice());
//                    }
                    Set<CoinInfoDto> collect = maxList[i].getBeforeCoinSet().stream().collect(Collectors.toSet());

                    beforeTotal.setCoins(collect);
//                    System.out.println("============ AFTER ============= ");
//                    for (CoinInfoDto coins : maxList[i].getAfterCoinSet()) {
//                        System.out.println("CoinName : " + coins.getCoinName() + ", CoinQuantity : " + coins.getCoinQuantity() + ", AvgPrice : " + coins.getAvgPrice());
//                    }
                    Set<CoinInfoDto> collect1 = maxList[i].getAfterCoinSet().stream().collect(Collectors.toSet());
                    afterTotal.setCoins(collect1);
                    buySell.setAfter(afterTotal);
                    buySell.setBefore(beforeTotal);
                    buySell.getBefore().setTotalKRW(maxList[i].getBeforeTotalKRW());
                    buySell.getAfter().setTotalKRW(maxList[i].getAfterTotalKRW());
                    buySell.setUserId(leader.getUserId());
                    kafkaProducer.sendBuySell(buySell);
                } else if(maxList[i].getSearch() != null) {
//                    System.out.println("Search = " + maxList[i].getSearch() + ", count = " + i);
//                    System.out.println("============ KRW ==============");
//                    for (CoinInfoDto coins : maxList[i].getAfterCoinSet()) {
//                        System.out.println("totalKRW : " + coins.getAvgPrice());
//                    }
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

    public List<Coin> balanceAPI(Leader leader) throws Exception {
        CryptUtil.Aes aes = CryptUtil.getAES();
        String decryptedSecretKey = aes.decrypt(key, leader.getSecretKey());

        Api_Client api = new Api_Client(leader.getApiKey(), decryptedSecretKey);

        HashMap<String, String> rgParams = new HashMap<String, String>();
        rgParams.put("currency", "ALL");

        String result = api.callApi("/info/balance", rgParams);
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(result);
        JSONObject jsonObj = (JSONObject) obj;
        String code = (String) jsonObj.get("status");
        List<CoinInfoDto> coins = new ArrayList<>();
        List<Coin> coinList = new ArrayList<>();

        if(code.equals("0000")){
            Map<String, String> data = new ObjectMapper().readValue(jsonObj.get("data").toString(), Map.class);
            data.entrySet().stream()
                    .filter(pair -> pair.getKey().contains("total_"))
                    .filter(pair -> Float.parseFloat(pair.getValue()) > 0.0)
                    .filter(pair -> !pair.getKey().equals("total_krw"))
                    .forEach(pair -> coins.add(new CoinInfoDto(pair.getKey().substring(6).toUpperCase(Locale.ROOT), Double.parseDouble(pair.getValue()))));
            coinList = coins.stream()
                    .map(coinInfoDto -> coinInfoDto.toCoin())
                    .collect(Collectors.toList());
        }
        return coinList;
    }

    public List<TransactionDto> transactionsAPI(Leader leader, String coinName) throws Exception {
        CryptUtil.Aes aes = CryptUtil.getAES();
        String decryptedSecretKey = aes.decrypt(key, leader.getSecretKey());

        Api_Client api = new Api_Client(leader.getApiKey(), decryptedSecretKey);

        HashMap<String, String> rgParams = new HashMap<String, String>();
        rgParams.put("order_currency", coinName);
        rgParams.put("payment_currency", "KRW");

        String result = api.callApi("/info/user_transactions", rgParams);
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(result);
        JSONObject jsonObj = (JSONObject) obj;
        String code = (String) jsonObj.get("status");
        List<TransactionDto> list = new ArrayList<>();
        if(code.equals("0000")){
            List<Map<String, Object>> data = new ObjectMapper().readValue(jsonObj.get("data").toString(), new TypeReference<List<Map<String, Object>>>() {
            });
            data.stream()
//                    .filter(d -> Long.parseLong(d.get("transfer_date").toString().substring(0,14)) >= leader.getLastTransTime())
                    .filter(d -> Long.parseLong(d.get("transfer_date").toString().substring(0,13)) >= 1L)
                    .forEach(d -> {
                        TransactionDto build = TransactionDto
                                .builder()
                                .search(d.get("search").toString())
                                .transfer_date(Long.parseLong(d.get("transfer_date").toString().substring(0,13)))
                                .units(Double.parseDouble(d.get("units").toString()))
                                .price(Double.parseDouble(d.get("price").toString()))
                                .build();
                        list.add(build);
                    });
            Collections.reverse(list);
            return list;
        }
        return list;
    }

    public int transMaxCount(Leader leader) throws Exception {
        List<TransactionDto> btc = transactionsAPI(leader, "BTC");
        int count = 0;
        for (TransactionDto tran:btc) {
            if(tran.getSearch().equals("4") || tran.getSearch().equals("5")){
                count++;
            }
        }
        return (count * 2) + 1;
    }

    public void transCoinInfo(MaxCoinTranDto[] maxList, Coin coin, List<TransactionDto> transactionDtos) {
        Leader leader = coin.getLeader();
        Double beforeLeaderKRW = leader.getTotalKRW();
        int count = 0;
        boolean isTrans = false;
        CoinInfoDto beforeCoinInfo = coin.toCoinDto();

        for (TransactionDto tran:transactionDtos) {
            if(tran.getSearch().equals("2")){
                coin.setCoinQuantity(coin.getCoinQuantity() - tran.getUnits());
                Double sell = tran.getUnits() * tran.getPrice();
                leader.setTotalKRW(leader.getTotalKRW() + sell);
                isTrans = true;
            }
            else if(tran.getSearch().equals("1")){
                Double quantity = coin.getCoinQuantity() + tran.getUnits();
                Double totalAmount = (coin.getAvgPrice() * coin.getCoinQuantity()) + (tran.getPrice() * tran.getUnits());
                Double avgPrice = totalAmount / quantity;
                coin.setCoinQuantity(quantity);
                coin.setAvgPrice(avgPrice);
                isTrans = true;
            }
            else {
                if(isTrans == true){
                    CoinInfoDto coinInfoDto = coin.toCoinDto();
                    Set<CoinInfoDto> afterCoinInfoSet = new HashSet<>();
                    Set<CoinInfoDto> beforeCoinInfoSet = new HashSet<>();
                    if(!maxList[count].getAfterCoinSet().isEmpty()){
                        afterCoinInfoSet = maxList[count].getAfterCoinSet();
                        beforeCoinInfoSet = maxList[count].getBeforeCoinSet();
                    }
                    afterCoinInfoSet.add(coinInfoDto);
                    beforeCoinInfoSet.add(beforeCoinInfo);
                    maxList[count].setAfterCoinSet(afterCoinInfoSet);
                    maxList[count].setBeforeCoinSet(beforeCoinInfoSet);
                    maxList[count].setSearch("1");
                    maxList[count].setBeforeTotalKRW(beforeLeaderKRW);
                    maxList[count].setAfterTotalKRW(leader.getTotalKRW());
                    beforeCoinInfo = coinInfoDto;
                    beforeLeaderKRW = leader.getTotalKRW();
                }
                count++;
                if(tran.getSearch().equals("4") && maxList[count].getAfterCoinSet().isEmpty()) {
                    CoinInfoDto krw = CoinInfoDto.builder()
                            .coinName("KRW")
                            .avgPrice(leader.getTotalKRW() + tran.getPrice())
                            .coinQuantity(0.0)
                            .build();
                    Set<CoinInfoDto> krwSet = new HashSet<>();
                    krwSet.add(krw);
                    maxList[count].setAfterCoinSet(krwSet);
                    maxList[count].setSearch("4");
                    leader.setTotalKRW(krw.getAvgPrice());
                }
                else if(tran.getSearch().equals("5") && maxList[count].getAfterCoinSet().isEmpty()) {
                    CoinInfoDto krw = CoinInfoDto.builder()
                            .coinName("KRW")
                            .avgPrice(leader.getTotalKRW() - tran.getPrice())
                            .coinQuantity(0.0)
                            .build();
                    Set<CoinInfoDto> krwSet = new HashSet<>();
                    krwSet.add(krw);
                    maxList[count].setAfterCoinSet(krwSet);
                    maxList[count].setSearch("5");
                    leader.setTotalKRW(krw.getAvgPrice());
                }
                count++;
                isTrans = false;
            }
        }
        if(isTrans == true){
            CoinInfoDto coinInfoDto = coin.toCoinDto();
            Set<CoinInfoDto> afterCoinInfoSet = new HashSet<>();
            Set<CoinInfoDto> beforeCoinInfoSet = new HashSet<>();
            if(!maxList[count].getAfterCoinSet().isEmpty()){
                afterCoinInfoSet = maxList[count].getAfterCoinSet();
                beforeCoinInfoSet = maxList[count].getBeforeCoinSet();
            }
            afterCoinInfoSet.add(coinInfoDto);
            beforeCoinInfoSet.add(beforeCoinInfo);
            maxList[count].setAfterCoinSet(afterCoinInfoSet);
            maxList[count].setBeforeCoinSet(beforeCoinInfoSet);
            maxList[count].setSearch("1");
            maxList[count].setBeforeTotalKRW(beforeLeaderKRW);
            maxList[count].setAfterTotalKRW(leader.getTotalKRW());
        }
        coinRepository.save(coin);
        if(beforeLeaderKRW.compareTo(leader.getTotalKRW()) != 0){
            leaderRepository.save(leader);
        }
    }
}