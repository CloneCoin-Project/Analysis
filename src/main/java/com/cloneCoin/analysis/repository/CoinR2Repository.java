package com.cloneCoin.analysis.repository;

import com.cloneCoin.analysis.domain.Coin;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface CoinR2Repository extends R2dbcRepository<Coin, Long> {

    @Query("select * from coins where leader_id = :user_id")
    Flux<Coin> findAllByUserId(Long user_id);
}
