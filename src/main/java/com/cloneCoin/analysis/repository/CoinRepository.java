package com.cloneCoin.analysis.repository;

import com.cloneCoin.analysis.domain.Coin;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CoinRepository extends MongoRepository<Coin, Long> {
}
