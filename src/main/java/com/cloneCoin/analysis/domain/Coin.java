package com.cloneCoin.analysis.domain;

import com.cloneCoin.analysis.dto.CoinInfoDto;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import javax.persistence.*;


@Entity
@Table("coins")
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Coin {

    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long id;

    @Column
    private String coinName;
    @Column
    private Double coinQuantity;
    @Column
    private Double avgPrice;
    @Column
    private Long userId;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "leader_id")
//    private Leader leader;

    public CoinInfoDto toCoinDto() {
        return CoinInfoDto.builder()
                .coinName(this.coinName)
                .coinQuantity(this.getCoinQuantity())
                .avgPrice(this.getAvgPrice())
                .build();
    }
}
