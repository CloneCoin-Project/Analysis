package com.cloneCoin.analysis.dto;

import lombok.Data;

import java.util.Map;

@Data
public class TransactionsDTO {

    private Long userId;
    private TotalDto before;
    private TotalDto after;
}
