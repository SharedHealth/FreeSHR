package org.freeshr.infrastructure.security;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.naming.AuthenticationException;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertTrue;

public class ClientAuthenticatorTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void shouldAuthenticateUser() throws Exception {
        String client_id = "123";
        String email = "email@gmail.com";
        String token = "xyz";

        UserAuthInfo userAuthInfo = new UserAuthInfo(client_id, email);
        UserInfo userInfo = getUserInfo(client_id, email, true, token);

        assertTrue(new ClientAuthenticator().authenticate(userAuthInfo, token, userInfo));
    }

    @Test
    public void shouldNotAuthenticateIfClientIsNotActivated() throws Exception {
        String client_id = "123";
        String email = "email@gmail.com";
        String token = "xyz";
        UserAuthInfo userAuthInfo = new UserAuthInfo(client_id, email);
        UserInfo userInfo = getUserInfo(client_id, email, false, token);

        thrown.expect(AuthenticationException.class);
        thrown.expectMessage("Client is not activated.");

        new ClientAuthenticator().authenticate(userAuthInfo, token, userInfo);
    }
    
    @Test
    public void shouldNotAuthenticateIfClientIdIsInvalid() throws Exception {
        String email = "email@gmail.com";
        String token = "xyz";
        UserAuthInfo userAuthInfo = new UserAuthInfo("123", email);
        UserInfo userInfo = getUserInfo("432", email, true, token);

        thrown.expect(AuthenticationException.class);
        thrown.expectMessage("Client ID is invalid.");

        new ClientAuthenticator().authenticate(userAuthInfo, token, userInfo);
    }

    @Test
    public void shouldNotAuthenticateIfTokenIsInvalid() throws Exception {
        String email = "email@gmail.com";
        String token = "xyz";
        String clientId = "123";

        UserAuthInfo userAuthInfo = new UserAuthInfo(clientId, email);
        UserInfo userInfo = getUserInfo(clientId, email, true, "abc");

        thrown.expect(AuthenticationException.class);
        thrown.expectMessage("Token is invalid or expired.");

        new ClientAuthenticator().authenticate(userAuthInfo, token, userInfo);
    }

    @Test
    public void shouldNotAuthenticateIfEmailIsInvalid() throws Exception {
        String email = "email@gmail.com";
        String token = "xyz";
        String clientId = "123";

        UserAuthInfo userAuthInfo = new UserAuthInfo(clientId, email);
        UserInfo userInfo = getUserInfo(clientId, "abc@gmail.com", true, token);

        thrown.expect(AuthenticationException.class);
        thrown.expectMessage("Email is invalid.");

        new ClientAuthenticator().authenticate(userAuthInfo, token, userInfo);
    }


    private UserInfo getUserInfo(String id, String email, boolean activated, String xyz) {
        return new UserInfo(id, "foo", email, 1, activated, xyz, asList(""), asList(new UserProfile("facility", "10000069", asList("3026"))));
    }
}