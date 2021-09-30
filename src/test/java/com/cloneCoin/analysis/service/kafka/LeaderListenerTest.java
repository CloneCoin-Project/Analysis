package com.cloneCoin.analysis.service.kafka;

import com.cloneCoin.analysis.config.CryptUtil;
import org.assertj.core.api.Assertions;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;

class LeaderListenerTest {

    @Test
    void 빗썸APITEST() throws ParseException {
        String URL = "https://api.bithumb.com/public/ticker/ALL";
        RestTemplate restTemplate = new RestTemplate();
        String forEntity = restTemplate.getForObject(URL, String.class);
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(forEntity);
        JSONObject jsonObj = (JSONObject) obj;
        String status = (String) jsonObj.get("status");
        Assertions.assertThat(status.equals("0000"));
    }

    @Test
    void 암복호화TEST() throws Exception {
        String key = "1234567890123456";
        String str = "142232521";
        CryptUtil.Aes aes = CryptUtil.getAES();
        String encryptedSecretKey = aes.encrypt(key, str);
        String decryptedSecretKey = aes.decrypt(key, encryptedSecretKey);
        Assertions.assertThat(decryptedSecretKey.equals(str));
    }

}