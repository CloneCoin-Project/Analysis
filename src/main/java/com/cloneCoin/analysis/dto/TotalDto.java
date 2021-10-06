package com.cloneCoin.analysis.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
public class TotalDto {

    // 코인들 정보와 잔액
    private List<CoinInfoDto> coins = new ArrayList<>();
    private Double totalKRW;

}
