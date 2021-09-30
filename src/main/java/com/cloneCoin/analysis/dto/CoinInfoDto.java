package com.cloneCoin.analysis.dto;

import com.cloneCoin.analysis.domain.Coin;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class CoinInfoDto {
    private String coinName;
    private Double coinQuantity;
    private Double avgPrice;

    public CoinInfoDto(String coinName, Double amount) {
        this.coinName = coinName;
        this.coinQuantity = amount;
    }

    public CoinInfoDto(String coinName, Double coinQuantity, Double avgPrice) {
        this.coinName = coinName;
        this.coinQuantity = coinQuantity;
        this.avgPrice = avgPrice;
    }

    public Coin toCoin(){
        return Coin.builder()
                .coinName(this.coinName)
                .coinQuantity(this.coinQuantity)
                .avgPrice(this.avgPrice)
                .build();
    }
}
