package com.cloneCoin.analysis.service;

import com.cloneCoin.analysis.domain.Coin;
import com.cloneCoin.analysis.domain.Leader;
import com.cloneCoin.analysis.dto.CoinInfoDto;
import com.cloneCoin.analysis.dto.LeadersDto;
import com.cloneCoin.analysis.repository.LeaderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeaderService {

    private final LeaderRepository leaderRepository;

    public List<LeadersDto> findAllLeaderCoins() {
        List<LeadersDto> leaders = new ArrayList<>();
        List<Leader> allLeaders = leaderRepository.findAll();
        allLeaders.stream()
                .forEach(leader -> {
                    LeadersDto leadersDto = new LeadersDto();
                    List<Coin> coinList = leader.getCoinList();
                    List<CoinInfoDto> collect = coinList.stream()
                            .map(coin -> coin.toCoinDto())
                            .collect(Collectors.toList());
                    leadersDto.setLeaderId(leader.getUserId());
                    leadersDto.setCoins(collect);
                    leaders.add(leadersDto);
                });
        return leaders;
    }
}
