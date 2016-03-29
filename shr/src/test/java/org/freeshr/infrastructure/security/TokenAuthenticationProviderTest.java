package org.freeshr.infrastructure.security;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.UUID;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class TokenAuthenticationProviderTest {

    @Mock
    IdentityServiceClient identityServiceClient;

    @Mock
    Authentication authentication;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    @Test
    public void shouldAuthenticateAgainstIdentityService() throws Exception {
        UUID token = UUID.randomUUID();
        UserAuthInfo userAuthInfo = new UserAuthInfo("123", "email@gmail.com", token.toString());

        when(authentication.getCredentials()).thenReturn(token.toString());
        when(authentication.getPrincipal()).thenReturn(userAuthInfo);
        when(identityServiceClient.authenticate(any(UserAuthInfo.class), eq(token.toString()))).thenReturn(tokenAuthentication(token));

        TokenAuthenticationProvider authenticationProvider = new TokenAuthenticationProvider(identityServiceClient);
        Authentication tokenAuthentication = authenticationProvider.authenticate(authentication);

        assertEquals("foo", tokenAuthentication.getName());
        assertEquals(getUserInfo(token).getProperties().getId(), ((UserInfo) tokenAuthentication.getPrincipal()).getProperties().getId());
        assertEquals(token.toString(), tokenAuthentication.getCredentials());
        assertTrue(tokenAuthentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_SHR User")));
        assertTrue(tokenAuthentication.isAuthenticated());
    }

    @Test(expected = BadCredentialsException.class)
    public void shouldRespond401OnException() throws Exception {
        UUID token = UUID.randomUUID();
        when(authentication.getPrincipal()).thenReturn(token.toString());
        when(identityServiceClient.authenticate(any(UserAuthInfo.class), eq(token.toString()))).thenThrow(new BadCredentialsException
                ("bar"));
        TokenAuthenticationProvider authenticationProvider = new TokenAuthenticationProvider
                (identityServiceClient);
        authenticationProvider.authenticate(authentication);
    }

    private TokenAuthentication tokenAuthentication(UUID token) {
        return new TokenAuthentication(getUserInfo(token), true);
    }

    private UserInfo getUserInfo(UUID token) {
        return new UserInfo("123", "foo", "email@gmail.com", 1, true,
                token.toString(), asList("MCI Admin", "SHR User"), asList(new UserProfile("facility", "10000069", asList("3026"))));
    }
}