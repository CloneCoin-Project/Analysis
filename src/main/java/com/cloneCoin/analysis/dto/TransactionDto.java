package com.cloneCoin.analysis.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TransactionDto {

    // Transaction 기록을 담는 DTO
    private String search;
    private Long transfer_date;
    private Double units;
    private Double price;

}
