package com.cloneCoin.analysis.dto;

import lombok.Data;

import java.util.List;

@Data
public class LeadersDto {

    private Long leaderId;
    private List<CoinInfoDto> coins;
}
