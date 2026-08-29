package com.example.et;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class EtApplication {

	public static void main(String[] args) {
		SpringApplication.run(EtApplication.class, args);
	}

}
