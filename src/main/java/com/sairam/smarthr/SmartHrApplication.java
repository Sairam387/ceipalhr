package com.sairam.smarthr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {
        "com.sairam.smarthr",
        "com.smarthr"
})
@EntityScan(basePackages = {
        "com.sairam.smarthr.entity",
        "com.smarthr.entity"
})
@EnableJpaRepositories(basePackages = {
        "com.sairam.smarthr.repository",
        "com.smarthr.repository"
})
@EnableScheduling
public class SmartHrApplication {

    public static void main(String[] args) {

        SpringApplication.run(
                SmartHrApplication.class,
                args
        );
    }
}