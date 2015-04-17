package org.freeshr.infrastructure.security;

import org.apache.log4j.Logger;
import org.freeshr.config.SHRProperties;
import org.freeshr.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
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
    private ClientAuthentication clientAuthentication;

    private Logger logger = Logger.getLogger(IdentityServiceClient.class);

    @Autowired
    public IdentityServiceClient(@Qualifier("SHRRestTemplate") AsyncRestTemplate shrRestTemplate,
                                 SHRProperties shrProperties,
                                 ClientAuthentication clientAuthentication) {
        this.shrRestTemplate = shrRestTemplate;
        this.shrProperties = shrProperties;
        this.clientAuthentication = clientAuthentication;
    }

    @Cacheable(value = "identityCache", unless = "#result == null")
    public TokenAuthentication authenticate(UserAuthInfo userAuthInfo, String token) throws AuthenticationException, ExecutionException,
            InterruptedException {
        String userInfoUrl = StringUtils.ensureSuffix(shrProperties.getIdentityServerBaseUrl(), "/") + token;
        HttpHeaders httpHeaders = getSHRIdentityHeaders(shrProperties);
        ListenableFuture<ResponseEntity<UserInfo>> listenableFuture = shrRestTemplate.exchange(userInfoUrl,
                HttpMethod.GET,
                new HttpEntity(httpHeaders), UserInfo.class);
        ResponseEntity<UserInfo> responseEntity = listenableFuture.get();
        if (!responseEntity.getStatusCode().is2xxSuccessful()) {
            logger.error(String.format("Unexpected response code %s from IDP while validating client %s with email %s and token %s",
                    responseEntity.getStatusCode(), userAuthInfo.getClientId(), userAuthInfo.getEmail(), userAuthInfo.getToken()));
            throw new AuthenticationServiceException("Unable to authenticate user.");
        }
        UserInfo userInfo = responseEntity.getBody();
        return new TokenAuthentication(userInfo, clientAuthentication.verify(userInfo, userAuthInfo, token));
    }
}
