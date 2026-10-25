package com.goodmaps;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class GoodMapsApplication {

    public static void main(String[] args) {
        SpringApplication.run(GoodMapsApplication.class, args);
    }
}
