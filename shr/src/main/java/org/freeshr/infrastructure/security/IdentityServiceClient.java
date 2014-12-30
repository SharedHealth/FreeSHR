package org.freeshr.infrastructure.security;

import org.freeshr.config.SHRProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.client.AsyncRestTemplate;

import javax.naming.AuthenticationException;
import java.util.concurrent.ExecutionException;

@Component
public class IdentityServiceClient {
    private AsyncRestTemplate shrRestTemplate;
    private SHRProperties shrProperties;

    @Autowired
    public IdentityServiceClient(@Qualifier("SHRRestTemplate") AsyncRestTemplate shrRestTemplate,
                                 SHRProperties shrProperties) {
        this.shrRestTemplate = shrRestTemplate;
        this.shrProperties = shrProperties;
    }

    public TokenAuthentication authenticate(String token) throws AuthenticationException, ExecutionException,
            InterruptedException {
        String userInfoUrl = shrProperties.getIdentityServerBaseUrl() + token;
        ListenableFuture<ResponseEntity<UserInfo>> listenableFuture = shrRestTemplate.exchange(userInfoUrl,
                HttpMethod.GET,
                HttpEntity.EMPTY, UserInfo.class);
        ResponseEntity<UserInfo> responseEntity = listenableFuture.get();
        if (!responseEntity.getStatusCode().is2xxSuccessful())
            throw new AuthenticationServiceException("Identity Server responded :" + responseEntity.getStatusCode()
                    .toString());
        UserInfo userInfo = responseEntity.getBody();
        return new TokenAuthentication(userInfo, token );
    }
}
