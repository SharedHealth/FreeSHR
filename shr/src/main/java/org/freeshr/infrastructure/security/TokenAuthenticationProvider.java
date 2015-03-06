package org.freeshr.infrastructure.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class TokenAuthenticationProvider implements AuthenticationProvider {
    private final static Logger logger = LoggerFactory.getLogger(TokenAuthenticationFilter.class);
    @Autowired
    private IdentityServiceClient identityServiceClient;

    public TokenAuthenticationProvider() {
    }

    public TokenAuthenticationProvider(IdentityServiceClient identityServiceClient) {
        this.identityServiceClient = identityServiceClient;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        try {
            return identityServiceClient.authenticate((UserAuthInfo) authentication.getPrincipal(), (String) authentication.getCredentials());
        }
        catch (AuthenticationException ex){
            logger.error(ex.getMessage());
            throw new BadCredentialsException(ex.getMessage());
        }
        catch (Exception ex){
            logger.error(ex.getMessage());
            throw new BadCredentialsException(ex.getMessage());
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(PreAuthenticatedAuthenticationToken.class);
    }
}
