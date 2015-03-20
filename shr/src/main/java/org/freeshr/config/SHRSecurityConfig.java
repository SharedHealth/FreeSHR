package org.freeshr.config;

import org.freeshr.infrastructure.security.TokenAuthenticationFilter;
import org.freeshr.infrastructure.security.TokenAuthenticationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.util.matcher.AndRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

import static org.freeshr.infrastructure.security.UserInfo.*;

@Configuration
@EnableWebSecurity
public class SHRSecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    private TokenAuthenticationProvider tokenAuthenticationProvider;

    private static final Logger logger = LoggerFactory.getLogger(SHRSecurityConfig.class);

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .anonymous().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        http
                .requestMatcher(new AndRequestMatcher(new ArrayList<RequestMatcher>() {
                    {
                        add(new NegatedRequestMatcher(new AntPathRequestMatcher(SHRProperties.DIAGNOSTICS_SERVLET_PATH)));
                        add(new AntPathRequestMatcher("/**"));
                    }
                }))
                .authorizeRequests()
                .regexMatchers(HttpMethod.GET, "\\/patients\\/.*\\/encounters(\\/.*){0,1}")
                    .hasAnyRole(SHR_FACILITY_GROUP, SHR_PROVIDER_GROUP, SHR_PATIENT_GROUP)
                .regexMatchers(HttpMethod.POST, "\\/patients\\/.*\\/encounters.*")
                    .hasAnyRole(SHR_FACILITY_GROUP, SHR_PROVIDER_GROUP)
                .regexMatchers(HttpMethod.GET, "\\/catchments\\/\\d.*\\/encounters.*")
                    .hasAnyRole(SHR_FACILITY_GROUP, SHR_PROVIDER_GROUP)
                .and()
                .addFilterBefore(new TokenAuthenticationFilter(authenticationManager()), BasicAuthenticationFilter
                        .class)
                .exceptionHandling().accessDeniedHandler(unauthorizedEntryPoint()).authenticationEntryPoint(unauthenticatedEntryPoint());
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    @Autowired
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(tokenAuthenticationProvider);
    }

    private AuthenticationEntryPoint unauthenticatedEntryPoint() {
        return new AuthenticationEntryPoint() {
            @Override
            public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException
                    authException) throws IOException, ServletException {
                logger.error(authException.getMessage());
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage());
            }
        };
    }

    private AccessDeniedHandler unauthorizedEntryPoint() {
        return new AccessDeniedHandler() {
            @Override
            public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
                logger.error(accessDeniedException.getMessage());
                response.sendError(HttpServletResponse.SC_FORBIDDEN, accessDeniedException.getMessage());
            }
        };
    }
}
