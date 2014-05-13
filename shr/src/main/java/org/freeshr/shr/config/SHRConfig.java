package org.freeshr.shr.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.web.client.RestTemplate;

@Configuration
@Import(SHRCassandraConfig.class)
@ComponentScan(basePackages = "org.freeshr.shr")
public class SHRConfig {

    @Bean(name = "SHRRestTemplate")
    public RestTemplate shrRestTemplate() {
        return new RestTemplate();
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }
}
