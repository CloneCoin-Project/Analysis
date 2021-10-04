package com.cloneCoin.analysis.scheduler;

import com.cloneCoin.analysis.config.aes.CryptUtil;
import com.cloneCoin.analysis.domain.Coin;
import com.cloneCoin.analysis.domain.Leader;
import com.cloneCoin.analysis.dto.CoinInfoDto;
import com.cloneCoin.analysis.dto.TransactionDto;
import com.cloneCoin.analysis.exception.InvalidKeysException;
import com.cloneCoin.analysis.service.Api_Client;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class ApiStep {

    @Value("${cryptutil.key}")
    private String key;

    public List<Coin> balanceAPI(Leader leader) throws Exception {
        Api_Client api = new Api_Client(leader.getApiKey(), decrypt(leader.getSecretKey()));

        HashMap<String, String> rgParams = new HashMap<String, String>();
        rgParams.put("currency", "ALL");

        String result = api.callApi("/info/balance", rgParams);
        if(result.contains("error")){
            throw new InvalidKeysException("apiKey or secretKey was wrong!");
        }
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
        Api_Client api = new Api_Client(leader.getApiKey(), decrypt(leader.getSecretKey()));

        HashMap<String, String> rgParams = new HashMap<String, String>();
        rgParams.put("order_currency", coinName);
        rgParams.put("payment_currency", "KRW");

        String result = api.callApi("/info/user_transactions", rgParams);
        if(result.contains("error")){
            throw new InvalidKeysException("apiKey or secretKey was wrong!!");
        }
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(result);
        JSONObject jsonObj = (JSONObject) obj;
        String code = (String) jsonObj.get("status");
        List<TransactionDto> list = new ArrayList<>();
        if(code.equals("0000")){
            List<Map<String, Object>> data = new ObjectMapper().readValue(jsonObj.get("data").toString(), new TypeReference<List<Map<String, Object>>>() {
            });
            data.stream()
//                    .filter(d -> Long.parseLong(d.get("transfer_date").toString().substring(0,13)) >= leader.getLastTransTime())
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

    private String decrypt(String cryptKey) throws Exception {
        CryptUtil.Aes aes = CryptUtil.getAES();
        String decryptedSecretKey = aes.decrypt(key, cryptKey);
        return decryptedSecretKey;
    }
}
