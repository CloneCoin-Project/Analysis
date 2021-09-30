package com.cloneCoin.analysis.repository;

import com.cloneCoin.analysis.domain.Leader;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LeaderR2Repository extends R2dbcRepository<Leader, Long> {
}
