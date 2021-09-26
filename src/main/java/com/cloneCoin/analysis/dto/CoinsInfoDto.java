package com.cloneCoin.analysis.dto;

import lombok.Data;

@Data
public class CoinsInfoDto {

    private String name;
    private Long amount;
    private int avgPrice;
}
