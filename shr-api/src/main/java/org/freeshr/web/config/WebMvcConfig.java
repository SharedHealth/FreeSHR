package org.freeshr.web.config;

import org.freeshr.config.SHRConfig;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

@Configuration
@EnableWebMvc
@Import(SHRConfig.class)
@ComponentScan(basePackages = "org.freeshr.web")
public class WebMvcConfig extends WebMvcConfigurationSupport {
}
