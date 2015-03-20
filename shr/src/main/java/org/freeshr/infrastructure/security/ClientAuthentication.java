package org.freeshr.infrastructure.security;

import org.springframework.stereotype.Component;

import javax.naming.AuthenticationException;

@Component
public class ClientAuthentication {

    public static final String CLIENT_IS_NOT_ACTIVATED = "Client is not activated.";
    public static final String TOKEN_IS_INVALID_OR_EXPIRED = "Token is invalid or expired.";
    public static final String CLIENT_ID_IS_INVALID = "Client Id is invalid.";
    public static final String EMAIL_IS_INVALID = "Email is invalid.";

    public boolean verify(UserInfo userInfo, UserAuthInfo userAuthInfo, String token) throws AuthenticationException {

        if (isInactiveUser(userInfo))
            throw new AuthenticationException(CLIENT_IS_NOT_ACTIVATED);
        else if (isInvalidToken(userInfo, token))
            throw new AuthenticationException(TOKEN_IS_INVALID_OR_EXPIRED);
        else if (isInvalidClient(userInfo, userAuthInfo))
            throw new AuthenticationException(CLIENT_ID_IS_INVALID);
        else if (isInvalidEmail(userInfo, userAuthInfo))
            throw new AuthenticationException(EMAIL_IS_INVALID);

        return true;
    }

    private boolean isInvalidEmail(UserInfo userInfo, UserAuthInfo userAuthInfo) {return !userAuthInfo.getEmail().equals(userInfo.getEmail());}

    private boolean isInvalidClient(UserInfo userInfo, UserAuthInfo userAuthInfo) {return !userAuthInfo.getClientId().equals(userInfo.getId());}

    private boolean isInvalidToken(UserInfo userInfo, String token) {return !token.equals(userInfo.getAccessToken());}

    private boolean isInactiveUser(UserInfo userInfo) {return !userInfo.isActivated() || userInfo.getIsActive() != 1;}
}
