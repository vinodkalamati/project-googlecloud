package com.stackroute.resultfetcher.controller;
import com.stackroute.resultfetcher.service.ResultFetcherService;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;

@RestController
@RequestMapping("/api")
public class ResultFetcherController {
    private ResultFetcherService resultFetcherService;
    @Autowired
    public ResultFetcherController(ResultFetcherService resultFetcherService) {
        this.resultFetcherService = resultFetcherService;
    }
    @GetMapping("result")
    @KafkaListener(topics = "QueryResult", groupId = "group_id", containerFactory = "kafkaListenerContainerFactory")
    public boolean resultToFrontEndController(String param) throws IOException, ParseException {
        resultFetcherService.resultToFrontEndService(param);
        return true;
    }
    @GetMapping("domainData")
    @KafkaListener(topics = "FrontEndQuery", groupId = "group_id", containerFactory = "kafkaListenerContainerFactory")
    public boolean domainDataToFrontEndController(String param){
         resultFetcherService.domainDataToFrontEndService(param);
         return true;
    }
}