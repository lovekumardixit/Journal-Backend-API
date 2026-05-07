package com.love.Backend;

import com.love.Backend.service.ElevenLabsService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.client.RestTemplate;

@EnableCaching
@EnableScheduling
@SpringBootApplication
@EnableTransactionManagement
public class BackendApplication {

	public static void main(String[] args) {

		SpringApplication.run(BackendApplication.class, args);
	}

	@Bean
	public PlatformTransactionManager ato(MongoDatabaseFactory dbfactory){
		return new MongoTransactionManager(dbfactory);
	}

    @Bean
    public RestTemplate restTemplate (){
        return new RestTemplate();
    }

}
