package com.dragonist.goodluckynews;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@PropertySource("classpath:application-oauth.properties")
public class GoodluckynewsApplication {

	public static void main(String[] args) {
		SpringApplication.run(GoodluckynewsApplication.class, args);
	}

}
