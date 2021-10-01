package com.cloneCoin.analysis.service.kafka;

import com.cloneCoin.analysis.config.aes.CryptUtil;
import com.cloneCoin.analysis.domain.Coin;
import com.cloneCoin.analysis.domain.Leader;
import com.cloneCoin.analysis.dto.*;
import com.cloneCoin.analysis.exception.InvalidKeysException;
import com.cloneCoin.analysis.repository.CoinR2Repository;
import com.cloneCoin.analysis.repository.LeaderR2Repository;
import com.cloneCoin.analysis.service.Api_Client;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class LeaderListener {

    @Value("${cryptutil.key}")
    private String key;
    private final LeaderR2Repository leaderR2Repository;
    private final CoinR2Repository coinR2Repository;

    @KafkaListener(topics = "user-kafka", groupId = "foo")
    public void ListenLeader(@Payload LeaderDto leader){
        leaderR2Repository.existsByUserId(leader.getLeaderId())
                .filter( i -> i == 0 )
                .subscribe(i -> {
                    String encryptedSecretKey = null;
                    try {
                        encryptedSecretKey = encrypt(leader.getSecretKey());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Leader newLeader = Leader.builder()
                            .userId(leader.getLeaderId())
                            .totalKRW(0.0)
                            .lastTransTime(System.currentTimeMillis())
                            .apiKey(leader.getApiKey())
                            .secretKey(encryptedSecretKey)
                            .build();
                    try {
                        if(balanceAPI(newLeader)){
                            log.info("Leader Created : " + leader);
                        }
                    } catch (Exception e) {
                        log.error(e.toString());
                    }});

    }

    public boolean balanceAPI(Leader leader) throws Exception {
        String decryptedSecretKey = decrypt(leader.getSecretKey());

        Api_Client api = new Api_Client(leader.getApiKey(), decryptedSecretKey);

        HashMap<String, String> rgParams = new HashMap<String, String>();
        rgParams.put("currency", "ALL");
        String result = api.callApi("/info/balance", rgParams);
        if(result.contains("error")){
            throw new InvalidKeysException("apiKey or secretKey was wrong!");
        }
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(result);
        JSONObject jsonObj = (JSONObject) obj;
        String s = (String) jsonObj.get("error");
        String code = (String) jsonObj.get("status");
        List<CoinInfoDto> coins = new ArrayList<>();
        if(code.equals("0000")){
            leaderR2Repository.save(leader).subscribe(i -> {
                Map<String, String> data = null;
                try {
                    data = new ObjectMapper().readValue(jsonObj.get("data").toString(), Map.class);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
                data.entrySet().stream()
                        .filter(pair -> pair.getKey().contains("total_"))
                        .filter(pair -> Float.parseFloat(pair.getValue()) > 0.0)
                        .forEach(pair -> coins.add(new CoinInfoDto(pair.getKey().substring(6).toUpperCase(Locale.ROOT), Double.parseDouble(pair.getValue()))));
                Map<String, Double> coinPrice = null;
                try {
                    coinPrice = getCoinPrice();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                List<Coin> coinList = new ArrayList<>();

                for (CoinInfoDto coinInfo: coins) {
                    if(coinInfo.getCoinName().equals("KRW")) {
                        leader.setTotalKRW( coinInfo.getCoinQuantity());
                    } else{
                        coinInfo.setAvgPrice(coinPrice.get(coinInfo.getCoinName()));
                        Coin coin = coinInfo.toCoin();
                        coin.setLeaderId(i.getUserId());
                        coinList.add(coin);
                    }
                }
                coinR2Repository.saveAll(coinList).subscribe();
            });
            return true;
        }
        return false;
    }

    private Map<String, Double> getCoinPrice() throws ParseException {
        String URL = "https://api.bithumb.com/public/ticker/ALL";
        RestTemplate restTemplate = new RestTemplate();
        String forEntity = restTemplate.getForObject(URL, String.class);
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(forEntity);
        JSONObject jsonObj = (JSONObject) obj;
        Object data = jsonObj.get("data");
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> map = objectMapper.convertValue(data, Map.class);
        Map<String, Double> coins = new HashMap<>();
        for (String key:map.keySet()) {
            if(key.equals("date")){
                continue;
            }
            Object o = map.get(key);
            Map coinInfo = objectMapper.convertValue(o, Map.class);
            coins.put(key, Double.parseDouble(coinInfo.get("closing_price").toString()));
        }
        return coins;
    }

    private String encrypt(String leaderSecret) throws Exception {
        String secretKey = leaderSecret;
        CryptUtil.Aes aes = CryptUtil.getAES();
        String encryptedSecretKey = aes.encrypt(key, secretKey);
        return encryptedSecretKey;
    }

    private String decrypt(String cryptKey) throws Exception {
        CryptUtil.Aes aes = CryptUtil.getAES();
        String decryptedSecretKey = aes.decrypt(key, cryptKey);
        return decryptedSecretKey;
    }
}
