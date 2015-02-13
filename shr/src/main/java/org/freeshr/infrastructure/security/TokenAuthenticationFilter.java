package org.freeshr.infrastructure.security;

import org.freeshr.config.SHRProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.springframework.util.StringUtils.isEmpty;


public class TokenAuthenticationFilter extends GenericFilterBean {
    private AuthenticationManager authenticationManager;
    private final static Logger logger = LoggerFactory.getLogger(TokenAuthenticationFilter.class);

    public TokenAuthenticationFilter(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }


    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {
        boolean shouldAuthenticateToken = true;
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        shouldAuthenticateToken = shouldAuthenticateToken(httpRequest);

        String token = httpRequest.getHeader(SHRProperties.SECURITY_TOKEN_HEADER);

        if (isEmpty(token) && shouldAuthenticateToken) {
            httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token not provided");
            return;
        }

        logger.debug("Authenticating token: {}", token);
        try {
            if(shouldAuthenticateToken) processTokenAuthentication(token);
            chain.doFilter(request, response);

        } catch (AuthenticationException ex) {
            SecurityContextHolder.clearContext();
            httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, ex.getMessage());
        }
    }

    private boolean shouldAuthenticateToken(HttpServletRequest httpRequest) {
        String servletPath = httpRequest.getServletPath();
        return isEmpty(servletPath) || !servletPath.contains(SHRProperties.DIAGNOSTICS_SERVLET_PATH);
    }

    private void processTokenAuthentication(String token) throws AuthenticationException {
        PreAuthenticatedAuthenticationToken authRequest = new PreAuthenticatedAuthenticationToken(token, null);
        Authentication authentication = authenticationManager.authenticate(authRequest);

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BadCredentialsException("Unable to authenticate provided token");
        }
        logger.debug("User successfully authenticated");
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
