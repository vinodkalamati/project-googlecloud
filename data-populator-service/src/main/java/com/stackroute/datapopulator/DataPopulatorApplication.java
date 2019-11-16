package com.stackroute.datapopulator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
@EnableEurekaClient
public class DataPopulatorApplication {

	public static void main(String[] args) {
		SpringApplication.run(DataPopulatorApplication.class, args);
	}

}
