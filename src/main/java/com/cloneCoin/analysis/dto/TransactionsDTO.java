package com.cloneCoin.analysis.dto;

import lombok.Data;

import java.util.Map;

@Data
public class TransactionsDTO {

    // BEFORE, AFTER 기록
    private Long userId;
    private TotalDto before;
    private TotalDto after;
}
