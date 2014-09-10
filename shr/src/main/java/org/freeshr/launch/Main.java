package org.freeshr.launch;

import org.freeshr.utils.cassandra.Migrations;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.ServletContextInitializer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import java.util.Map;

import static java.lang.Integer.valueOf;
import static java.lang.System.getenv;

@Configuration
public class Main {

    @Bean
    public EmbeddedServletContainerFactory getFactory() {
        Map<String, String> env = getenv();
        TomcatEmbeddedServletContainerFactory factory = new TomcatEmbeddedServletContainerFactory();
        factory.addInitializers(new ServletContextInitializer() {
            @Override
            public void onStartup(ServletContext servletContext) throws ServletException {
                ServletRegistration.Dynamic shr = servletContext.addServlet("shr", DispatcherServlet.class);
                shr.addMapping("/");
                shr.setInitParameter("contextClass", "org.springframework.web.context.support.AnnotationConfigWebApplicationContext");
                shr.setInitParameter("contextConfigLocation", "org.freeshr.launch.WebMvcConfig");
                shr.setAsyncSupported(true);
            }
        });
        String bdshr_port = env.get("BDSHR_PORT");
        factory.setPort(valueOf(bdshr_port));
        return factory;
    }


    public static void main(String[] args) throws Exception {
        new Migrations().migrate();
        SpringApplication.run(Main.class, args);
    }
}
