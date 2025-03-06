package com.draconist.goodluckynews;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@EnableScheduling
@EnableJpaAuditing
@SpringBootApplication
public class GoodluckynewsApplication {

	public static void main(String[] args) {
		SpringApplication.run(GoodluckynewsApplication.class, args);
	}

}
