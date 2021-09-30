package com.cloneCoin.analysis.web;

import com.cloneCoin.analysis.dto.LeadersDto;
import com.cloneCoin.analysis.service.BithumbAPIService;
import com.cloneCoin.analysis.service.kafka.KafkaProducer;
import com.cloneCoin.analysis.service.LeaderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/analysis")
    public class MainController {

    private final BithumbAPIService bithumbAPIService;
    private final KafkaProducer kafkaProducer;
    private final LeaderService leaderService;

    @PostMapping("/kafka")
    public String kafkaTest() {
        System.out.println("test");
        kafkaProducer.sendTest();
        return "OK";
    }

    @GetMapping("/schedule")
    public List<LeadersDto> schedule(){
        return leaderService.findAllLeaderCoins();
    }
}
