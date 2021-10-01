package com.cloneCoin.analysis.converter;

import com.cloneCoin.analysis.domain.Coin;
import com.cloneCoin.analysis.domain.Leader;
import com.cloneCoin.analysis.dto.Row;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

@ReadingConverter
public class LeaderReadConverter implements Converter<Row, Coin> {
    @Override
    public Coin convert(Row source) {
        Leader.builder()
                .userId(source.getUserId())
                .apiKey(source.getApiKey())
                .secretKey(source.getSecretKey())
                .totalKRW(source.getTotalKRW())
                .lastTransTime(source.getLastTransTime())
                .build();
        return Coin.builder()
                .coinName(source.getCoinName())
                .coinQuantity(source.getCoinQuantity())
                .avgPrice(source.getAvgPrice())
                .userId(source.getUserId())
                .build();
    }
}
