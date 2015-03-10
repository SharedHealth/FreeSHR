package org.freeshr.infrastructure.security;

import org.freeshr.config.SHRProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.client.AsyncRestTemplate;

import javax.naming.AuthenticationException;
import java.util.concurrent.ExecutionException;

import static org.freeshr.utils.HttpUtil.getSHRIdentityHeaders;

@Component
public class IdentityServiceClient {
    private AsyncRestTemplate shrRestTemplate;
    private SHRProperties shrProperties;
    private ClientAuthenticator clientAuthenticator;

    @Autowired
    public IdentityServiceClient(@Qualifier("SHRRestTemplate") AsyncRestTemplate shrRestTemplate,
                                 SHRProperties shrProperties, ClientAuthenticator clientAuthenticator) {
        this.shrRestTemplate = shrRestTemplate;
        this.shrProperties = shrProperties;
        this.clientAuthenticator = clientAuthenticator;
    }

    public TokenAuthentication authenticate(UserAuthInfo userAuthInfo, String token) throws AuthenticationException, ExecutionException,
            InterruptedException {
        String userInfoUrl = shrProperties.getIdentityServerBaseUrl() + token;
        HttpHeaders httpHeaders = getSHRIdentityHeaders(shrProperties);
        ListenableFuture<ResponseEntity<UserInfo>> listenableFuture = shrRestTemplate.exchange(userInfoUrl,
                HttpMethod.GET,
                new HttpEntity(httpHeaders), UserInfo.class);
        ResponseEntity<UserInfo> responseEntity = listenableFuture.get();
        if (!responseEntity.getStatusCode().is2xxSuccessful())
            throw new AuthenticationServiceException("Identity Server responded :" + responseEntity.getStatusCode()
                    .toString());
        UserInfo userInfo = responseEntity.getBody();
        boolean isAuthenticated = clientAuthenticator.authenticate(userAuthInfo, token, userInfo);
        return new TokenAuthentication(userInfo, isAuthenticated);
    }
}
