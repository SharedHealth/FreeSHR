package org.freeshr.shr.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.client.RestTemplate;

@Configuration
@Import(SHRCassandraConfig.class)
@ComponentScan(basePackages = "org.freeshr.shr")
public class SHRConfig {

    @Bean(name = "SHRRestTemplate")
    public RestTemplate shrRestTemplate() {
        return new RestTemplate();
    }
}
