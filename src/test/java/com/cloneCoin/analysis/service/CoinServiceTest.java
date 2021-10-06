package com.cloneCoin.analysis.service;

import com.cloneCoin.analysis.domain.Coin;
import com.cloneCoin.analysis.repository.CoinRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CoinServiceTest {

    @Autowired
    CoinRepository coinRepository;

    @Test
    void CoinRepoTest() {
        Coin btc = Coin.builder()
                .coinName("BTC")
                .coinQuantity(1.2)
                .avgPrice(114.2)
                .build();
        coinRepository.save(btc);
        Coin coin = coinRepository.findById(1L).orElseThrow();
        Assertions.assertThat(btc.getCoinName().equals(coin.getCoinName()));
    }
}
