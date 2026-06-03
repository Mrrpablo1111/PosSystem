package com.sh.sh.pos.system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@SpringBootApplication
@EnableCaching
@EnableScheduling
@EnableMethodSecurity
public class ShPosSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShPosSystemApplication.class, args);
	}

}
