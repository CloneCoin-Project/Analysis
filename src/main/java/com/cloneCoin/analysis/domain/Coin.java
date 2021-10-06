package com.cloneCoin.analysis.domain;

import com.cloneCoin.analysis.dto.CoinInfoDto;
import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "coins")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Coin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "coin_id")
    private Long id;

    private String coinName;
    private Double coinQuantity;
    private Double avgPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leader_id")
    private Leader leader;

    public CoinInfoDto toCoinDto() {
        return CoinInfoDto.builder()
                .coinName(this.coinName)
                .coinQuantity(this.getCoinQuantity())
                .avgPrice(this.getAvgPrice())
                .build();
    }
}
