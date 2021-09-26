package com.cloneCoin.analysis.domain;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Table(name = "coins")
public class Coin {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "coin_id")
    private Long id;

    private String coinName;
    private Long amount;
    private int avgPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leader_id")
    private Leader leader;
}
