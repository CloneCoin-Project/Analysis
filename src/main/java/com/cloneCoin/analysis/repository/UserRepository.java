package com.cloneCoin.analysis.repository;


import com.cloneCoin.analysis.domain.User;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface UserRepository extends R2dbcRepository<User, Long> {
}
