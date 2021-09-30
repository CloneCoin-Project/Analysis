package com.cloneCoin.analysis.web;

import com.cloneCoin.analysis.service.LeaderHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class AnalysisRoutes {
    
    @Bean
    public RouterFunction<ServerResponse> router(LeaderHandler leaderHandler) {
        return RouterFunctions.route()
                .GET("/analysis/getall", request -> leaderHandler.findAllLeadersCoins())
                .POST("/analysis/kafka", request -> leaderHandler.kafka()) /** PRODUCER TEST */
                .build();
    }
}
