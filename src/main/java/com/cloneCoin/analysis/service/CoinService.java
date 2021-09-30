package com.cloneCoin.analysis.service;

import com.cloneCoin.analysis.domain.Coin;
import com.cloneCoin.analysis.repository.CoinRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CoinService {

    private final CoinRepository coinRepository;

    public List<Coin> findAll(){
        return coinRepository.findAll();
    }
}
