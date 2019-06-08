package me.rocyang.cloud.eurekastart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@EnableEurekaServer
@SpringBootApplication
public class EurekaStartApplication {
    public static void main(String[] args) {
        SpringApplication.run(EurekaStartApplication.class, args);
    }
}
