package org.freeshr.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.AsyncRestTemplate;

@Configuration
@Import(SHRCassandraConfig.class)
@ComponentScan(basePackages = "org.freeshr")
public class SHRConfig {

    @Autowired
    private SHRProperties shrProperties;

    @Bean(name = "SHRRestTemplate")
    public AsyncRestTemplate shrRestTemplate() {
        /*TODO: See whether ThreadPoolExecutor is the best for the job*/
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(shrProperties.getRestPoolSize());
        return new AsyncRestTemplate(executor);
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }
}
