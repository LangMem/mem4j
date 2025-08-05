package com.mem0.configs;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(MemoryConfig.class)
public class MemoryAutoConfiguration {
}
