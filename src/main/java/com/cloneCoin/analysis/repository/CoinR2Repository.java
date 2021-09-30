package com.cloneCoin.analysis.repository;

import com.cloneCoin.analysis.domain.Coin;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CoinR2Repository extends R2dbcRepository<Coin, Long> {
}
