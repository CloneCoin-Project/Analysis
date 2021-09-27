package com.cloneCoin.analysis.repository;

import com.cloneCoin.analysis.domain.Leader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LeaderRepository extends JpaRepository<Leader, Long> {

}
