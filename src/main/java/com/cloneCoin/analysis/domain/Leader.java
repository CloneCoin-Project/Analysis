package com.cloneCoin.analysis.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@org.springframework.data.relational.core.mapping.Table("leaders")
@NoArgsConstructor
@AllArgsConstructor
public class Leader {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long leader_id;

    @Column
    private Long userId;
    @Column
    private Double totalKRW;
    @Column
    private Long lastTransTime;
    @Column
    private String apiKey;
    @Column
    private String secretKey;

    @Builder
    public Leader(Long userId, Double totalKRW, Long lastTransTime, String apiKey, String secretKey) {
        this.userId = userId;
        this.totalKRW = totalKRW;
        this.lastTransTime = lastTransTime;
        this.apiKey = apiKey;
        this.secretKey = secretKey;
    }

}
