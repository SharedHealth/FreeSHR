package org.freeshr.shr.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(SHRCassandraConfig.class)
@ComponentScan(basePackages = "org.freeshr.shr")
public class SHRConfig {
}
