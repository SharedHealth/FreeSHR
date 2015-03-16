package org.freeshr.infrastructure.security;

import org.freeshr.utils.HttpUtil;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.UUID;

import static java.util.Arrays.asList;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;
import static org.freeshr.utils.HttpUtil.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class TokenAuthenticationFilterTest {
    @Mock
    AuthenticationManager authenticationManager;
    @Mock
    Authentication authentication;
    @Mock
    HttpServletRequest request;
    @Mock
    HttpServletResponse response;
    @Mock
    FilterChain chain;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    @Test
    public void shouldFilterOutRequestsWithoutToken() throws Exception {
        when(request.getHeader(AUTH_TOKEN_KEY)).thenReturn("");
        when(request.getHeader(CLIENT_ID_KEY)).thenReturn("1232");
        when(request.getHeader(FROM_KEY)).thenReturn("email@gmail.com");
        TokenAuthenticationFilter tokenAuthenticationFilter = new TokenAuthenticationFilter(authenticationManager);
        tokenAuthenticationFilter.doFilter(request, response, chain);
        verify(authenticationManager, never()).authenticate(any(Authentication.class));
        verify(chain, never()).doFilter(request, response);
        verify(response, times(1)).sendError(SC_UNAUTHORIZED, "Headers are incomplete");
    }

    @Test
    public void shouldFilterOutRequestsWithoutClientId() throws Exception {
        when(request.getHeader(AUTH_TOKEN_KEY)).thenReturn("zdfed");
        when(request.getHeader(CLIENT_ID_KEY)).thenReturn("");
        when(request.getHeader(FROM_KEY)).thenReturn("email@gmail.com");
        TokenAuthenticationFilter tokenAuthenticationFilter = new TokenAuthenticationFilter(authenticationManager);
        tokenAuthenticationFilter.doFilter(request, response, chain);
        verify(authenticationManager, never()).authenticate(any(Authentication.class));
        verify(chain, never()).doFilter(request, response);
        verify(response, times(1)).sendError(SC_UNAUTHORIZED, "Headers are incomplete");
    }

    @Test
    public void shouldFilterOutRequestsWithoutEmail() throws Exception {
        when(request.getHeader(AUTH_TOKEN_KEY)).thenReturn("acv");
        when(request.getHeader(CLIENT_ID_KEY)).thenReturn("1232");
        when(request.getHeader(FROM_KEY)).thenReturn("");
        TokenAuthenticationFilter tokenAuthenticationFilter = new TokenAuthenticationFilter(authenticationManager);
        tokenAuthenticationFilter.doFilter(request, response, chain);
        verify(authenticationManager, never()).authenticate(any(Authentication.class));
        verify(chain, never()).doFilter(request, response);
        verify(response, times(1)).sendError(SC_UNAUTHORIZED, "Headers are incomplete");
    }

    @Test
    public void shouldFilterOutRequestsWithInvalidToken() throws Exception {
        String invalidToken = UUID.randomUUID().toString();
        when(request.getHeader(HttpUtil.AUTH_TOKEN_KEY)).thenReturn(invalidToken);
        when(request.getHeader(CLIENT_ID_KEY)).thenReturn("1232");
        when(request.getHeader(FROM_KEY)).thenReturn("email@gmail.com");
        when(authenticationManager.authenticate(any(Authentication.class))).thenThrow(new BadCredentialsException
                ("bar"));

        TokenAuthenticationFilter tokenAuthenticationFilter = new TokenAuthenticationFilter(authenticationManager);
        try {
            tokenAuthenticationFilter.doFilter(request, response, chain);
        } catch (Exception ex) {
            assertEquals(ex.getMessage(), "bar");
        }
        verify(chain, never()).doFilter(request, response);
        verify(response, times(1)).sendError(SC_UNAUTHORIZED, "bar");
    }

    @Test
    public void shouldSetAuthenticationAndPropagateChainOnSuccess() throws Exception {
        UUID token = UUID.randomUUID();
        when(request.getHeader(HttpUtil.AUTH_TOKEN_KEY)).thenReturn(token.toString());
        when(request.getHeader(CLIENT_ID_KEY)).thenReturn("1232");
        when(request.getHeader(FROM_KEY)).thenReturn("email@gmail.com");
        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(tokenAuthentication(token));
        TokenAuthenticationFilter tokenAuthenticationFilter = new TokenAuthenticationFilter(authenticationManager);
        tokenAuthenticationFilter.doFilter(request, response, chain);
        verify(authenticationManager, times(1)).authenticate(any(Authentication.class));
        verify(chain, times(1)).doFilter(request, response);
        assertEquals("foo", SecurityContextHolder.getContext().getAuthentication().getName());
    }

    private TokenAuthentication tokenAuthentication(UUID token) {
        return new TokenAuthentication(new UserInfo("1232", "foo", "email@gmail.com", 1, true,
                token.toString(), new ArrayList<String>(), asList(new UserProfile("facility", "10000069", asList("3026")))), true);
    }
}