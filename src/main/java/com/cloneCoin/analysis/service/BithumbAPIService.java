package com.cloneCoin.analysis.service;

import com.cloneCoin.analysis.config.CryptUtil;
import com.cloneCoin.analysis.domain.Coin;
import com.cloneCoin.analysis.domain.Leader;
import com.cloneCoin.analysis.dto.*;
import com.cloneCoin.analysis.repository.LeaderRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@RequiredArgsConstructor
public class BithumbAPIService {

    private final LeaderRepository leaderRepository;

    @Value("${cryptutil.key}")
    private String key;

    public boolean apiTest(Long leaderId) throws Exception {
        Api_Client api = createApiClient(leaderId);

        HashMap<String, String> rgParams = new HashMap<String, String>();
        rgParams.put("order_currency", "BTC");

        String result = api.callApi("/info/wallet_address", rgParams);
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(result);
        JSONObject jsonObj = (JSONObject) obj;
        String code = (String) jsonObj.get("status");
        if(code.equals("0000")){
            return true;
        }
        return false;
    }

    public List<CoinInfoDto> balanceAPI(Long leaderId) throws Exception {
        Leader leader = leaderRepository.findById(leaderId).orElseThrow();
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
        }
        for (Coin coin:leader.getCoinList()
             ) {
            System.out.println(coin.getCoinName());
        }
        return coins;
    }

    public String transactionsAPI(Long leaderId, String coinName) throws Exception {
        Leader leader = leaderRepository.findById(leaderId).orElseThrow();
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

        if(code.equals("0000")){
            List<Map<String, Object>> data = new ObjectMapper().readValue(jsonObj.get("data").toString(), new TypeReference<List<Map<String, Object>>>() {
            });
            data.stream()
                    .filter(d -> Long.parseLong(d.get("transfer_date").toString()) >= leader.getLastTransTime())
                    .forEach(d -> System.out.println(d));

            return result;
        }
        return "None";
    }

    private Api_Client createApiClient(long leaderId) throws Exception {
        Leader leader = leaderRepository.findById(leaderId).orElseThrow();
        CryptUtil.Aes aes = CryptUtil.getAES();
        String decryptedSecretKey = aes.decrypt(key, leader.getSecretKey());

        Api_Client api = new Api_Client(leader.getApiKey(), decryptedSecretKey);

        return api;
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
