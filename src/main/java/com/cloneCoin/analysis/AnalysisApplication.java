package com.cloneCoin.analysis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class AnalysisApplication {

	public static void main(String[] args) {
		SpringApplication.run(AnalysisApplication.class, args);
	}

}
