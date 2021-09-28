package com.cloneCoin.analysis.domain;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "coins")
public class Coin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "coin_id")
    private Long id;

    private String coinName;
    private Long amount;
    private int avgPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leader_id")
    private Leader leader;
}
