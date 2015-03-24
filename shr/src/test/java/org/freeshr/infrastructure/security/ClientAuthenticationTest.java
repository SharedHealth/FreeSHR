package org.freeshr.infrastructure.security;

import org.junit.Test;

import javax.naming.AuthenticationException;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertTrue;

public class ClientAuthenticationTest {

    @Test
    public void shouldAuthenticateUser() throws Exception {
        String client_id = "123";
        String email = "email@gmail.com";
        String token = "xyz";

        UserAuthInfo userAuthInfo = new UserAuthInfo(client_id, email, token);
        UserInfo userInfo = getUserInfo(client_id, email, true, token);

        assertTrue(new ClientAuthentication().verify(userInfo, userAuthInfo, token));
    }

    @Test(expected = AuthenticationException.class)
    public void shouldNotAuthenticateIfClientIsNotActivated() throws Exception {
        String client_id = "123";
        String email = "email@gmail.com";
        String token = "xyz";
        UserAuthInfo userAuthInfo = new UserAuthInfo(client_id, email, token);
        UserInfo userInfo = getUserInfo(client_id, email, false, token);

        new ClientAuthentication().verify(userInfo, userAuthInfo, token);
    }

    @Test(expected = AuthenticationException.class)
    public void shouldNotAuthenticateIfClientIdIsInvalid() throws Exception {
        String email = "email@gmail.com";
        String token = "xyz";
        UserAuthInfo userAuthInfo = new UserAuthInfo("123", email, token);
        UserInfo userInfo = getUserInfo("432", email, true, token);

        new ClientAuthentication().verify(userInfo, userAuthInfo, token);
    }

    @Test(expected = AuthenticationException.class)
    public void shouldNotAuthenticateIfTokenIsInvalid() throws Exception {
        String email = "email@gmail.com";
        String token = "xyz";
        String clientId = "123";

        UserAuthInfo userAuthInfo = new UserAuthInfo(clientId, email, token);
        UserInfo userInfo = getUserInfo(clientId, email, true, "abc");

        new ClientAuthentication().verify(userInfo, userAuthInfo, token);
    }

    @Test(expected = AuthenticationException.class)
    public void shouldNotAuthenticateIfEmailIsInvalid() throws Exception {
        String email = "email@gmail.com";
        String token = "xyz";
        String clientId = "123";

        UserAuthInfo userAuthInfo = new UserAuthInfo(clientId, email, token);
        UserInfo userInfo = getUserInfo(clientId, "abc@gmail.com", true, token);

        new ClientAuthentication().verify(userInfo, userAuthInfo, token);
    }


    private UserInfo getUserInfo(String id, String email, boolean activated, String xyz) {
        return new UserInfo(id, "foo", email, 1, activated, xyz, asList(""),
                asList(new UserProfile("facility", "10000069", asList("3026"))));
    }
}