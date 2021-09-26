package com.cloneCoin.analysis.dto;

import lombok.Data;

import java.util.Map;

@Data
public class TransactionsDTO {

    private String type;
    private TotalDto before;
    private TotalDto after;
}
