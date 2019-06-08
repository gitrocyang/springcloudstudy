package me.rocyang.cloud.eurekaclientstart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class EurekaClientStartApplication {

    public static void main(String[] args) {
        SpringApplication.run(EurekaClientStartApplication.class, args);
    }

}
