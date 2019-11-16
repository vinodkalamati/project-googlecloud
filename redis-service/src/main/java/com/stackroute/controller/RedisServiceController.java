package com.stackroute.controller;
import com.stackroute.domain.DataModel;
import com.stackroute.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class RedisServiceController {
    @Autowired
    private RedisService redisService;
    public RedisServiceController(RedisService redisService) {
        this.redisService = redisService;
    }


    // The controller will send data to kafka Topic which is used for data Population
    @PostMapping
    @KafkaListener(topics = "SchedulerResult", groupId = "group_id", containerFactory = "kafkaListenerContainerFactory")
    public boolean RedisService(String param) {
        redisService.populate(param);
        return true;
    }

    @GetMapping
    public Map<String, DataModel> all() {
        return redisService.getAll();
    }
}
