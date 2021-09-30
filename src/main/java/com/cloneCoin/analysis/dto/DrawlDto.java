package com.cloneCoin.analysis.dto;

import lombok.Data;

@Data
public class DrawlDto {

    // 입금, 출금 기록
    private Long userId;
    private String type;
    private Double totalKRW;
}
