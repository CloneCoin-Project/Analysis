package com.cloneCoin.analysis.dto;

import lombok.Data;

@Data
public class CoinInfoDto {
    private String coinName;
    private Float amount;

    public CoinInfoDto(String coinName, Float amount) {
        this.coinName = coinName;
        this.amount = amount;
    }
}
