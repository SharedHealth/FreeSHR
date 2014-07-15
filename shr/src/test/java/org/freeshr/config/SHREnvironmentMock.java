package org.freeshr.config;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.mock.env.MockPropertySource;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

@Configuration
public class SHREnvironmentMock implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        MutablePropertySources propertySources = applicationContext.getEnvironment().getPropertySources();
        MockPropertySource mockEnvVars = new MockPropertySource();
        try {
            InputStream inputStream = this.getClass().getResourceAsStream("/test-shr.properties");
            Properties properties = new Properties();
            properties.load(inputStream);
            for (Object property : properties.keySet()) {
                mockEnvVars.setProperty(property.toString(), properties.getProperty(property.toString()));
            }
            propertySources.replace(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME, mockEnvVars);
        } catch (Exception ignored) {
            // Do nothing
        }
    }
}
