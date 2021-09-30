package com.cloneCoin.analysis.dto;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class TotalDto {

    private Set<CoinInfoDto> coins = new HashSet<>();
    private Double totalKRW;

}
