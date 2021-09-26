package com.cloneCoin.analysis.domain;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "leaders")
public class Leader {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Long userId;

    private String apiKey;

    private String secretKey;
}
