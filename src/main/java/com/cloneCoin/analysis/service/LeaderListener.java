package com.cloneCoin.analysis.service;

import com.cloneCoin.analysis.config.CryptUtil;
import com.cloneCoin.analysis.domain.Coin;
import com.cloneCoin.analysis.domain.Leader;
import com.cloneCoin.analysis.dto.CoinInfoDto;
import com.cloneCoin.analysis.dto.LeaderDto;
import com.cloneCoin.analysis.repository.LeaderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class LeaderListener {

    @Value("${cryptutil.key}")
    private String key;

    private final LeaderRepository leaderRepository;

    @KafkaListener(topics = "user-kafka", groupId = "foo")
    public void ListenLeader(LeaderDto leader) throws Exception {
        String strToEncrypt = leader.getSecretKey();
        CryptUtil.Aes aes = CryptUtil.getAES();
        String encryptedSecretKey = aes.encrypt(key, strToEncrypt);

        Leader newLeader = Leader.builder()
                .userId(leader.getLeaderId())
                .totalKRW(0.0)
                .lastTransTime(0L)
                .apiKey(leader.getApiKey())
                .secretKey(encryptedSecretKey)
                .build();
        if(balanceAPI(newLeader)){
            log.info(String.format("User created -> %s", newLeader));
        } else {
            log.info("User Info was Wrong!!");
        }
    }

    public boolean balanceAPI(Leader leader) throws Exception {
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

        if(code.equals("0000")){
            Map<String, String> data = new ObjectMapper().readValue(jsonObj.get("data").toString(), Map.class);
            data.entrySet().stream()
                    .filter(pair -> pair.getKey().contains("total_"))
                    .filter(pair -> Float.parseFloat(pair.getValue()) > 0.0)
                    .filter(pair -> !pair.getKey().equals("total_krw"))
                    .forEach(pair -> coins.add(new CoinInfoDto(pair.getKey().substring(6).toUpperCase(Locale.ROOT), Double.parseDouble(pair.getValue()))));
            Map<String, Double> coinPrice = getCoinPrice();
            List<Coin> coinList = new ArrayList<>();
            for (CoinInfoDto coinInfo: coins) {
                Double price = coinPrice.get(coinInfo.getCoinName());
                coinInfo.setAvgPrice(price);
                Coin coin = coinInfo.toCoin();
                coin.setLeader(leader);
                coinList.add(coin);
            }
            leader.setCoinList(coinList);
            for (Coin coin:leader.getCoinList()) {
                System.out.println("COINNAME : " + coin.getCoinName() + ", " + "COINQUANTITY : " + coin.getCoinQuantity() + ", " + "AVGPRICE : " + coin.getAvgPrice());
            }
            leaderRepository.save(leader);
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

}
