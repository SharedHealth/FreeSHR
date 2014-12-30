package org.freeshr.infrastructure.security;

import org.freeshr.config.SHRProperties;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.SettableListenableFuture;
import org.springframework.web.client.AsyncRestTemplate;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class IdentityServiceClientTest {
    @Mock
    AsyncRestTemplate asyncRestTemplate;
    @Mock
    SHRProperties shrProperties;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    @Test
    public void shouldCallIdentityServerToAuthenticate() throws Exception {
        String token = UUID.randomUUID().toString();
        when(shrProperties.getIdentityServerBaseUrl()).thenReturn("foo/");
        when(asyncRestTemplate.exchange("foo/" + token, HttpMethod.GET, new HttpEntity(null),
                UserInfo.class)).thenReturn(createResponse(token, HttpStatus.OK));
        TokenAuthentication tokenAuthentication = new IdentityServiceClient(asyncRestTemplate,
                shrProperties).authenticate(token);

        assertEquals(tokenAuthentication.getPrincipal().toString(), token);
        assertEquals(tokenAuthentication.getName(), "bar");
        assertEquals(tokenAuthentication.getAuthorities().size(), 2);
    }

    @Test(expected = AuthenticationServiceException.class)
    public void shouldFailIfIdentityServerGetFails() throws Exception {
        String token = UUID.randomUUID().toString();
        when(shrProperties.getIdentityServerBaseUrl()).thenReturn("foo/");
        when(asyncRestTemplate.exchange("foo/" + token, HttpMethod.GET, new HttpEntity(null),
                UserInfo.class)).thenReturn(createResponse(token, HttpStatus.UNAUTHORIZED));
        new IdentityServiceClient(asyncRestTemplate,
                shrProperties).authenticate(token);
    }

    private ListenableFuture<ResponseEntity<UserInfo>> createResponse(String token, HttpStatus statusCode) {
        SettableListenableFuture<ResponseEntity<UserInfo>> response = new
                SettableListenableFuture<>();
        response.set(new ResponseEntity<>(userInfo(token), statusCode));
        return response;
    }

    private UserInfo userInfo(String token) {
        return new UserInfo("bar", Arrays.asList("MCI_ADMIN", "SHR_USER"));
    }
}