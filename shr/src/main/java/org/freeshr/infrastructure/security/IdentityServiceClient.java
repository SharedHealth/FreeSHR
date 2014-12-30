package org.freeshr.infrastructure.security;

import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Component;

import javax.naming.AuthenticationException;

@Component
public class IdentityServiceClient {
    public TokenAuthentication authenticate(String token) throws AuthenticationException {
        return new TokenAuthentication("foo", token, AuthorityUtils.commaSeparatedStringToAuthorityList
                ("ROLE_SHR_USER"));
    }
}
