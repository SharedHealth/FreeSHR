package org.freeshr.config;

import org.apache.http.client.RedirectStrategy;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.client.HttpComponentsAsyncClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.AsyncRestTemplate;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

@Configuration
@Import({SHRSecurityConfig.class, SHRCassandraConfig.class,
        SHRCacheConfiguration.class, ActuatorConfig.class})
@ComponentScan(basePackages = {"org.freeshr.config",
        "org.freeshr.infrastructure",
        "org.freeshr.interfaces",
        "org.freeshr.domain",
        "org.freeshr.application.fhir",
        "org.freeshr.utils",
        "org.freeshr.validations"})
public class SHRConfig {
    @Autowired
    private SHRProperties shrProperties;

    @Bean(name = "SHRRestTemplate")
    public AsyncRestTemplate shrRestTemplate() {
        try {
            /*TODO: See whether ThreadPoolExecutor is the best for the job*/
            TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;
            SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom()
                    .loadTrustMaterial(null, acceptingTrustStrategy)
                    .build();

            CloseableHttpAsyncClient httpAsyncClient = HttpAsyncClients.custom()
                    .setRedirectStrategy(new LaxRedirectStrategy())
                    .setSSLContext(sslContext)
                    .setSSLHostnameVerifier((s, sslSession) -> true)
                    .build();

            HttpComponentsAsyncClientHttpRequestFactory httpRequestFactory = new HttpComponentsAsyncClientHttpRequestFactory(httpAsyncClient);
            return new AsyncRestTemplate(httpRequestFactory);
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            e.printStackTrace();
        }
        return new AsyncRestTemplate();
    }

    private void setRequestTimeOuts(SimpleClientHttpRequestFactory asyncRequestFactory) {
        asyncRequestFactory.setConnectTimeout(shrProperties.getServerConnectionTimeout());
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    /*TODO: Verify whether the codes are needed for validations by writing tests and then remove this bean if they
    are not.*/
    @Bean(name = "hl7CodeProperties")
    public static PropertiesFactoryBean hl7() {
        PropertiesFactoryBean bean = new PropertiesFactoryBean();
        bean.setLocation(new ClassPathResource("/hl7codes.properties"));
        return bean;
    }
}
