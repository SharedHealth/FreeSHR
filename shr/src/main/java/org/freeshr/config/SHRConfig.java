package org.freeshr.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.AsyncRestTemplate;

@Configuration
@Import(SHRCassandraConfig.class)
@ComponentScan(basePackages = {"org.freeshr.config",
        "org.freeshr.infrastructure",
        "org.freeshr.interfaces",
        "org.freeshr.domain",
        "org.freeshr.application.fhir"})
public class SHRConfig {

    @Autowired
    private SHRProperties shrProperties;

    @Bean(name = "SHRRestTemplate")
    public AsyncRestTemplate shrRestTemplate() {
        /*TODO: See whether ThreadPoolExecutor is the best for the job*/
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.initialize();
        executor.setCorePoolSize(shrProperties.getRestPoolSize());
        return new AsyncRestTemplate(executor);
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    /*TODO: Verify whether the codes are needed for validations by writing tests and then remove this bean if they are not.*/
    @Bean(name = "hl7CodeProperties")
    public static PropertiesFactoryBean hl7() {
        PropertiesFactoryBean bean = new PropertiesFactoryBean();
        bean.setLocation(new ClassPathResource("/hl7codes.properties"));
        return bean;
    }
}
