package com.cloneCoin.analysis.dto;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class MaxCoinTranDto {

    // Trans 기록을 담는 DTO
    private String search;
    private Set<CoinInfoDto> beforeCoinSet = new HashSet<>();
    private Set<CoinInfoDto> afterCoinSet = new HashSet<>();
    private Double beforeTotalKRW;
    private Double afterTotalKRW;
}
