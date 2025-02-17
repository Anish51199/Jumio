package com.sms.smsservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class SmsserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(SmsserviceApplication.class, args);
	}

}
