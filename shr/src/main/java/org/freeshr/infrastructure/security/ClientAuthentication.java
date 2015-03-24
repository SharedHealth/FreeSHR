package org.freeshr.infrastructure.security;

import org.springframework.stereotype.Component;

import javax.naming.AuthenticationException;

@Component
public class ClientAuthentication {

    public boolean verify(UserInfo userInfo, UserAuthInfo userAuthInfo, String token) throws AuthenticationException {
        String exceptionMessage = "User credentials is invalid";
        if (isInactiveUser(userInfo)) {
            throw new AuthenticationException(exceptionMessage);
        }
        else if (isInvalidToken(userInfo, token))
            throw new AuthenticationException(exceptionMessage);
        else if (isInvalidClient(userInfo, userAuthInfo))
            throw new AuthenticationException(exceptionMessage);
        else if (isInvalidEmail(userInfo, userAuthInfo))
            throw new AuthenticationException(exceptionMessage);

        return true;
    }

    private boolean isInvalidEmail(UserInfo userInfo, UserAuthInfo userAuthInfo) {return !userAuthInfo.getEmail().equals(userInfo.getEmail());}

    private boolean isInvalidClient(UserInfo userInfo, UserAuthInfo userAuthInfo) {return !userAuthInfo.getClientId().equals(userInfo.getId());}

    private boolean isInvalidToken(UserInfo userInfo, String token) {return !token.equals(userInfo.getAccessToken());}

    private boolean isInactiveUser(UserInfo userInfo) {return !userInfo.isActivated() || userInfo.getIsActive() != 1;}
}
