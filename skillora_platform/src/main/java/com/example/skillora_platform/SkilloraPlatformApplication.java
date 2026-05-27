package com.example.skillora_platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class SkilloraPlatformApplication {

	public static void main(String[] args) {
		SpringApplication.run(SkilloraPlatformApplication.class, args);
	}

}
