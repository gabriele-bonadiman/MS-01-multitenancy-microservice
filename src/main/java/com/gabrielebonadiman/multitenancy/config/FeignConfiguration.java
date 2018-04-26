package com.gabrielebonadiman.multitenancy.config;

import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = "com.gabrielebonadiman.multitenancy")
public class FeignConfiguration {

}
