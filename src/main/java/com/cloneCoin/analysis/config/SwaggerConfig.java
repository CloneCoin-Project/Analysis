package com.cloneCoin.analysis.config;

import io.swagger.v3.oas.models.parameters.Parameter;
import org.springdoc.core.GroupedOpenApi;
import org.springdoc.core.customizers.OpenApiCustomiser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public GroupedOpenApi analysisGroupApi() {
        return GroupedOpenApi.builder()
                .group("analysis")
                .pathsToMatch("/analysis/**")
                .build();
    }

}
