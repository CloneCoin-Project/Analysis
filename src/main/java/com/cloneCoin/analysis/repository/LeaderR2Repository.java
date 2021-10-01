package com.cloneCoin.analysis.repository;

import com.cloneCoin.analysis.domain.Leader;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

public interface LeaderR2Repository extends R2dbcRepository<Leader, Long> {

    @Query("select case when count(e.user_id) > 0 then true else false end from leaders e where e.user_id=:userId")
    Mono<Integer> existsByUserId(Long userId);
}
