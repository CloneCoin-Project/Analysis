package com.cloneCoin.analysis.web;

import com.cloneCoin.analysis.dto.ApiKeyDto;
import com.cloneCoin.analysis.dto.CoinInfoDto;
import com.cloneCoin.analysis.dto.LeaderIdDto;
import com.cloneCoin.analysis.dto.LeadersDto;
import com.cloneCoin.analysis.service.BithumbAPIService;
import com.cloneCoin.analysis.service.KafkaProducer;
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

    @PostMapping("/apitest")
    public String apiTest(@RequestBody LeaderIdDto leaderIdDto) throws Exception {
        if(bithumbAPIService.apiTest(leaderIdDto.getLeaderId())){
            return "Ok";
        }else{
            return "None";
        }
    }


    @PostMapping("/balance")
    public List<CoinInfoDto> balance(@RequestBody LeaderIdDto leaderIdDto) throws Exception {
        return bithumbAPIService.balanceAPI(leaderIdDto.getLeaderId());
    }

    @PostMapping("/transactions")
    public String transactions(@RequestBody ApiKeyDto apiKeyDto) throws Exception {
        return bithumbAPIService.transactionsAPI(apiKeyDto.getLeaderId(), apiKeyDto.getCoinName());
    }

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
