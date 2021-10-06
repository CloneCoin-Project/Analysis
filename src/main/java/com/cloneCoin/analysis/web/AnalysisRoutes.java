package com.cloneCoin.analysis.web;

import com.cloneCoin.analysis.dto.LeadersDto;
import com.cloneCoin.analysis.service.LeaderHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class AnalysisRoutes {

    @Bean
    @RouterOperations(
            {
                    @RouterOperation(path = "/analysis/getall", produces = {
                            MediaType.APPLICATION_JSON_VALUE},
                            beanClass = LeaderHandler.class, method = RequestMethod.GET, beanMethod = "findAllLeadersCoins",
                            operation = @Operation(operationId = "findAllLeadersCoins", responses = {
                                    @ApiResponse(responseCode = "200", description = "successful operation",
                                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = LeadersDto.class)))),
                                    @ApiResponse(responseCode = "404", description = "Leader not found")
                            }))
            }
    )
    public RouterFunction<ServerResponse> router(LeaderHandler leaderHandler) {
        return RouterFunctions.route()
                .GET("/analysis/getall", request -> leaderHandler.findAllLeadersCoins())
                .POST("/analysis/kafka", request -> leaderHandler.kafka()) /** PRODUCER TEST */
                .build();
    }

}
