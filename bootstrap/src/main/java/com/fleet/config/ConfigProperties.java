package com.fleet.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import com.fleet.infrastructure.config.FleetProperties;

@Component
@EnableConfigurationProperties
public class ConfigProperties {

    @Bean
    FleetProperties fleetProperties() {
        return new FleetProperties();
    }

}
