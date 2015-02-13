package org.freeshr.config;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.actuate.trace.InMemoryTraceRepository;
import org.springframework.boot.actuate.trace.TraceRepository;
import org.springframework.boot.actuate.trace.WebRequestTraceFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.Servlet;
import javax.servlet.ServletRegistration;

@ConditionalOnClass({ Servlet.class, DispatcherServlet.class, ServletRegistration.class })
@Configuration
public class ActuatorConfig {

    @Bean
    public TraceRepository traceRepository() {
        return new InMemoryTraceRepository();
    }

    @Bean
    public WebRequestTraceFilter webRequestLoggingFilter(BeanFactory beanFactory) {
        WebRequestTraceFilter filter = new WebRequestTraceFilter(traceRepository());
        filter.setDumpRequests(true);
        return filter;
    }
}
