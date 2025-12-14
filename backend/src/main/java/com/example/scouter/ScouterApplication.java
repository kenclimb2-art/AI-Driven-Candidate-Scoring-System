package com.example.scouter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(exclude = {KafkaAutoConfiguration.class})
@ComponentScan(basePackages = "com.example.scouter")
public class ScouterApplication {

	public static void main(String[] args) {
		SpringApplication.run(ScouterApplication.class, args);
	}

}
