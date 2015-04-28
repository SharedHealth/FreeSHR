package org.freeshr.infrastructure.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.naming.AuthenticationException;

@Component
public class ClientAuthentication {

    private final static Logger logger = LoggerFactory.getLogger(ClientAuthentication.class);

    public boolean verify(UserInfo userInfo, UserAuthInfo userAuthInfo, String token) throws AuthenticationException {
        String exceptionMessage = "User credentials is invalid";
        if (isInactiveUser(userInfo) ||
                isInvalidToken(userInfo, token) ||
                isInvalidClient(userInfo, userAuthInfo) ||
                isInvalidEmail(userInfo, userAuthInfo)) {
            logger.debug(String.format("Token %s doesn't belong to client %s with email %s", token, userAuthInfo.getClientId(), userAuthInfo.getEmail()));
            throw new AuthenticationException(exceptionMessage);
        }
        return true;
    }

    private boolean isInvalidEmail(UserInfo userInfo, UserAuthInfo userAuthInfo) {
        return !userAuthInfo.getEmail().equals(userInfo
                .getProperties().getEmail());
    }

    private boolean isInvalidClient(UserInfo userInfo, UserAuthInfo userAuthInfo) {
        return !userAuthInfo.getClientId().equals(userInfo
                .getProperties().getId());
    }

    private boolean isInvalidToken(UserInfo userInfo, String token) {
        return !token.equals(userInfo.getProperties().getAccessToken());
    }

    private boolean isInactiveUser(UserInfo userInfo) {
        return !userInfo.getProperties().isActivated() || userInfo.getProperties().getIsActive() != 1;
    }
}
