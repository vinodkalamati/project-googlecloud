package com.stackroute.datapopulator.controller;
import com.stackroute.datapopulator.domain.ConvertedDataModel;
import com.stackroute.datapopulator.service.DataFetcherService;
import com.stackroute.datapopulator.service.DataPopulatorService;
import com.stackroute.datapopulator.service.NlpResultConverterService;
import com.stackroute.datapopulator.service.QueryTriggerService;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api")
public class DataPopulatorController {
    private DataPopulatorService dataPopulatorService;
    private DataFetcherService dataFetcherService;
    private QueryTriggerService queryTriggerService;
    private NlpResultConverterService nlpResultConverterService;
    @Autowired
    public DataPopulatorController(DataPopulatorService dataPopulatorService, DataFetcherService dataFetcherService,QueryTriggerService queryTriggerService,NlpResultConverterService nlpResultConverterService) {
        this.dataPopulatorService = dataPopulatorService;
        this.dataFetcherService = dataFetcherService;
        this.queryTriggerService=queryTriggerService;
        this.nlpResultConverterService=nlpResultConverterService;
    }
    //------------------to populate the database-------------------------
    @PostMapping("data")
    @KafkaListener(topics = "WikiScrapper", groupId = "group_id", containerFactory = "kafkaListenerContainerFactory")
    public boolean dataPopulatorController(String param) throws IOException {
        boolean flag=false;
        Object flag1=dataPopulatorService.dataPopulator(param.toLowerCase());
        if(flag1.toString().equals("null")){flag=dataPopulatorService.dataMerger();}
        else{
            flag=dataPopulatorService.dataMerger();
            dataFetcherService.dataToRedisService();
            ConvertedDataModel cdm=(ConvertedDataModel)flag1;
	    System.out.println(cdm.toString());
            queryTriggerService.queryServiceTrigger(cdm.getQuery(),cdm.getDomain());
        }
        if (flag==true){return true;}
        else return false;
    }
    //------------------to fetch domain info from database-------------------------
    @GetMapping
    @KafkaListener(topics = "SchedulerFlag", groupId = "group_id", containerFactory = "kafkaListenerContainerFactory")
    public boolean dataFetcherController(String param){
        dataFetcherService.dataToRedisService();
        return true;
    }
    @PostMapping("frontend")
    @KafkaListener(topics = "frontendData", groupId = "group_id", containerFactory = "kafkaListenerContainerFactory")
    public boolean dataFromFrontEndController(String param){
        if(dataPopulatorService.dataFromFrontEndService(param)){
            dataPopulatorService.dataMerger();
        }
        return true;
    }

    @PostMapping("nlpresult")
    @KafkaListener(topics = "NlpResult", groupId = "group_id", containerFactory = "kafkaListenerContainerFactory")
    public boolean dataFromNlpController(String param) throws ParseException {
        nlpResultConverterService.nlpResultConverter(param.toLowerCase());
        return true;
    }
}
