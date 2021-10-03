package com.cloneCoin.analysis.dto;

import lombok.Data;

@Data
public class LeaderDto {

    // Leader의 UserId, apiKey, 암호화된 secretKey
    private Long leaderId;
    private String leaderName;
    private String apiKey;
    private String secretKey;
}
