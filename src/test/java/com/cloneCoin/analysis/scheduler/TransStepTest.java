package com.cloneCoin.analysis.scheduler;

import com.cloneCoin.analysis.domain.Coin;
import com.cloneCoin.analysis.domain.Leader;
import com.cloneCoin.analysis.dto.CoinInfoDto;
import com.cloneCoin.analysis.dto.MaxCoinTranDto;
import com.cloneCoin.analysis.dto.TransactionDto;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

@SpringBootTest
class TransStepTest {

    Leader createLeader() {
        Leader leader = Leader.builder()
                .userId(1L)
                .totalKRW(3000.0)
                .lastTransTime(0L)
                .apiKey("123123")
                .secretKey("123123")
                .build();
        Coin btc = Coin.builder()
                .coinName("BTC")
                .coinQuantity(1.2)
                .avgPrice(12314.4)
                .build();
        List<Coin> coins = new ArrayList<>();
        coins.add(btc);
        leader.setCoinList(coins);
        return leader;
    }

    TransactionDto createTran(String search) {
        return TransactionDto.builder()
                .search(search)
                .transfer_date(1234123L)
                .price(30.0)
                .units(0.2)
                .build();
    }

    @Test
    void 입출금거래() {
        MaxCoinTranDto maxCoinTranDto = new MaxCoinTranDto();
        Leader leader = createLeader();
        TransactionDto tran = createTran("4");
        CoinInfoDto krw = CoinInfoDto.builder()
                .coinName("KRW")
                .avgPrice(leader.getTotalKRW() - tran.getPrice())
                .coinQuantity(0.0)
                .build();
        Assertions.assertThat(krw.getAvgPrice().equals(2970.0));
    }

    @Test
    void 트랜젝션에따른메시징(){
        List<Integer[]> test = new ArrayList<>();
        test.add(new Integer[]{1,2,1,4,1,2,4,1});
        test.add(new Integer[]{4,4});
        test.add(new Integer[]{1,2,4,1,1,4});
        test.add(new Integer[]{4,4,1,2});
        test.add(new Integer[]{1,4,4,1,2});
        test.add(new Integer[]{1,2,4,4});
        test.add(new Integer[]{4,1,2,4,1,2});

        List<List<Integer>> after = new ArrayList<>();
        after.add(Arrays.asList(1,4,1,4,1));
        after.add(Arrays.asList(4,4));
        after.add(Arrays.asList(1,4,1,4));
        after.add(Arrays.asList(4,4,1));
        after.add(Arrays.asList(1,4,4,1));
        after.add(Arrays.asList(1,4,4));
        after.add(Arrays.asList(4,1,4,1));

        List<List<Integer>> answer = new ArrayList<>();
        for (Integer[] t:test) {
            boolean isTrue = false;
            List<Integer> result = new ArrayList<>();
            for (int i:t) {
                if(i == 1){
                    isTrue = true;
                }
                else if(i == 2){
                    isTrue = true;
                }
                else{
                    if(isTrue == true){
                        result.add(1);
                    }
                    result.add(4);
                    isTrue = false;
                }
            }
            if(isTrue == true){
                result.add(1);
            }
            answer.add(result);
        }
        assertThat(answer, is(after));
    }
}