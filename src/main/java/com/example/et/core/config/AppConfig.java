package com.example.et.core.config;

import com.example.et.core.config.props.JwtProps;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(JwtProps.class)
public class AppConfig {
}
