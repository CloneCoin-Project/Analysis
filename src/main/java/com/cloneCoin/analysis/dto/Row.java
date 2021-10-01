package com.cloneCoin.analysis.dto;

import lombok.Data;
import org.springframework.data.relational.core.mapping.Column;

@Data
public class Row {

    private Long userId;
    private Double totalKRW;
    private Long lastTransTime;
    private String apiKey;
    private String secretKey;
    private String coinName;
    private Double coinQuantity;
    private Double avgPrice;
}
