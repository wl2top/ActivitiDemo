package com.wl.top;

import org.activiti.spring.boot.SecurityAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
public class ActivitidemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(ActivitidemoApplication.class, args);
    }

}
