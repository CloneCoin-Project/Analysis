package com.cloneCoin.analysis.domain;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Builder
@Entity
@Getter
@Setter
@Table(name = "leaders")
@NoArgsConstructor
@AllArgsConstructor
public class Leader {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "leader_id")
    private Long id;

    private Long userId;

    private Double totalKRW;

    private Long lastTransTime;

    private String apiKey;

    private String secretKey;

    @OneToMany(mappedBy = "leader", cascade = CascadeType.ALL)
    private List<Coin> coinList = new ArrayList<>();

}
