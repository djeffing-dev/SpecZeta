package com.djeffing.SpecZeta;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class SpecZetaApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpecZetaApplication.class, args);
	}

}
