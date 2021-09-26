package com.cloneCoin.analysis.repository;

import com.cloneCoin.analysis.domain.Leader;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LeaderRepository extends MongoRepository<Long, Leader> {

}
