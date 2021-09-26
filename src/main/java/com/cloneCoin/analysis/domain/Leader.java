package com.cloneCoin.analysis.domain;

import lombok.Data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "leaders")
public class Leader {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "leader_id")
    private Long id;

    private Long userId;

    private String apiKey;

    private String secretKey;

    @OneToMany(mappedBy = "leader", cascade = CascadeType.ALL)
    private List<Coin> coinList = new ArrayList<>();
}
