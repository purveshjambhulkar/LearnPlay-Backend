package com.example.LNPbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import org.modelmapper.ModelMapper;  // Importing ModelMapper for object mapping
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootApplication
public 	class LnPbackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(LnPbackendApplication.class, args);
	}

	@Bean
	public ModelMapper modelMapper() {
		return new ModelMapper();  // Bean for ModelMapper to map between DTOs and entities
	}

	@Bean
	public WebClient webClient() {
		return WebClient.builder().build();
	}

}
