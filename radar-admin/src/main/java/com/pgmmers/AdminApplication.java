package com.pgmmers;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication(exclude = MongoAutoConfiguration.class)
@MapperScan("com.pgmmers.radar.mapper")
public class AdminApplication
{
    public static void main( String[] args ){
        SpringApplication.run(AdminApplication.class, args);
    }
}
