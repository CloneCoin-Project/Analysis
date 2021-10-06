package com.cloneCoin.analysis.dto;

import com.cloneCoin.analysis.domain.Type;
import lombok.Builder;
import lombok.Data;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Data
public class PushDto {

    private Long leaderId;
    private String type;
    private String message;

    @Builder
    public PushDto(Long leaderId, String type, String message) {
        this.leaderId = leaderId;
        this.type = type;
        this.message = message;
    }
}
