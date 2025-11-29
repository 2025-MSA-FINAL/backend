package com.popspot.popupplatform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class PopupplatformApplication {

	public static void main(String[] args) {
		SpringApplication.run(PopupplatformApplication.class, args);
	}

}
