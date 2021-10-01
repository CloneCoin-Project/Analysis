package com.cloneCoin.analysis.dto;

import lombok.Data;

import java.util.List;

@Data
public class LeadersDto {

    // Leader의 userId와 코인들 정보
    private Long leaderId;
    private List<CoinInfoDto> coins;

    public LeadersDto(Long leaderId, List<CoinInfoDto> coins) {
        this.leaderId = leaderId;
        this.coins = coins;
    }
}
