package org.freeshr.infrastructure.security;

import org.freeshr.config.SHRProperties;
import org.freeshr.utils.HttpUtil;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.SettableListenableFuture;
import org.springframework.web.client.AsyncRestTemplate;

import java.util.UUID;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

public class IdentityServiceClientTest {
    @Mock
    AsyncRestTemplate asyncRestTemplate;
    @Mock
    SHRProperties shrProperties;
    @Mock
    private ClientAuthentication clientAuthentication;
    private HttpHeaders httpHeaders;

    @Before
    public void setUp() throws Exception {
        initMocks(this);

        httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpUtil.CLIENT_ID_KEY, "123");
        httpHeaders.add(HttpUtil.AUTH_TOKEN_KEY, "xyz");
    }

    @Test
    public void shouldCallIdentityServerToAuthenticate() throws Exception {
        String token = UUID.randomUUID().toString();
        UserAuthInfo userAuthInfo = new UserAuthInfo("123", "email@gmail.com");

        when(shrProperties.getIdPClientId()).thenReturn("123");
        when(shrProperties.getIdPAuthToken()).thenReturn("xyz");
        when(shrProperties.getIdentityServerBaseUrl()).thenReturn("foo/");
        when(asyncRestTemplate.exchange("foo/" + token, GET, new HttpEntity(httpHeaders),
                UserInfo.class)).thenReturn(createResponse(token, OK));
        when(clientAuthentication.verify(userInfo(token), userAuthInfo, token)).thenReturn(true);

        TokenAuthentication tokenAuthentication = new IdentityServiceClient(asyncRestTemplate,
                shrProperties, clientAuthentication).authenticate(userAuthInfo, token);

        assertEquals(tokenAuthentication.getCredentials().toString(), token);
        UserInfo expectedUserInfo = userInfo(token);
        assertEquals(((UserInfo) tokenAuthentication.getPrincipal()).getId(), expectedUserInfo.getId());
        assertEquals(((UserInfo) tokenAuthentication.getPrincipal()).getAccessToken(), expectedUserInfo.getAccessToken());
        assertEquals(((UserInfo) tokenAuthentication.getPrincipal()).getEmail(), expectedUserInfo.getEmail());
        assertEquals(((UserInfo) tokenAuthentication.getPrincipal()).getIsActive(), expectedUserInfo.getIsActive());
        assertEquals(((UserInfo) tokenAuthentication.getPrincipal()).isActivated(), expectedUserInfo.isActivated());
        assertEquals(tokenAuthentication.getName(), expectedUserInfo.getName());
        assertEquals(tokenAuthentication.getName(), "bar");
        assertEquals(tokenAuthentication.getAuthorities().size(), 2);
        assertEquals(tokenAuthentication.getAuthorities().size(), 2);
    }

    @Test(expected = AuthenticationServiceException.class)
    public void shouldFailIfIdentityServerGetFails() throws Exception {
        String token = UUID.randomUUID().toString();
        UserAuthInfo userAuthInfo = new UserAuthInfo("123", "email@gmail.com");

        when(shrProperties.getIdPClientId()).thenReturn("123");
        when(shrProperties.getIdPAuthToken()).thenReturn("xyz");
        when(shrProperties.getIdentityServerBaseUrl()).thenReturn("foo/");
        when(asyncRestTemplate.exchange("foo/" + token, GET, new HttpEntity(httpHeaders),
                UserInfo.class)).thenReturn(createResponse(token, UNAUTHORIZED));
        new IdentityServiceClient(asyncRestTemplate,
                shrProperties, clientAuthentication).authenticate(userAuthInfo, token);
    }

    @Test
    public void shouldCallClientAuthenticator() throws Exception {
        String token = UUID.randomUUID().toString();
        UserAuthInfo userAuthInfo = new UserAuthInfo("123", "email@gmail.com");
        UserInfo userInfo = userInfo(token);

        when(shrProperties.getIdPClientId()).thenReturn("123");
        when(shrProperties.getIdPAuthToken()).thenReturn("xyz");
        when(shrProperties.getIdentityServerBaseUrl()).thenReturn("foo/");
        when(asyncRestTemplate.exchange("foo/" + token, GET, new HttpEntity(httpHeaders),
                UserInfo.class)).thenReturn(createResponse(token, OK));
        when(clientAuthentication.verify(userInfo, userAuthInfo, token)).thenReturn(true);

        new IdentityServiceClient(asyncRestTemplate,
                shrProperties, clientAuthentication).authenticate(userAuthInfo, token);

        verify(clientAuthentication,times(1)).verify(any(UserInfo.class), eq(userAuthInfo), eq(token));
    }

    private ListenableFuture<ResponseEntity<UserInfo>> createResponse(String token, HttpStatus statusCode) {
        SettableListenableFuture<ResponseEntity<UserInfo>> response = new
                SettableListenableFuture<>();
        response.set(new ResponseEntity<>(userInfo(token), statusCode));
        return response;
    }

    private UserInfo userInfo(String token) {
        return new UserInfo("123", "bar", "email@gmail.com", 1, true,
                token, asList("MCI_ADMIN", "SHR_USER"), asList(new UserProfile("facility", "10000069", asList("3026"))));
    }
}