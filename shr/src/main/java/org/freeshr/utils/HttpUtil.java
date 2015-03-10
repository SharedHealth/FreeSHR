package org.freeshr.utils;


import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpRequest;
import org.freeshr.config.SHRProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.Charset;
import java.util.Arrays;

public class HttpUtil {

    public static final String CLIENT_ID_KEY = "client_id";
    public static final String AUTH_TOKEN_KEY = "X-Auth-Token";
    public static final String FROM_KEY = "From";

    public static MultiValueMap<String, String> basicAuthHeaders(String userName, String password) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        String auth = userName + ":" + password;
        headers.add("Authorization", "Basic " + new String(Base64.encodeBase64(auth.getBytes(Charset.forName("UTF-8")
        ))));
        return headers;
    }

    public static HttpHeaders getSHRIdentityHeaders(SHRProperties shrProperties) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(CLIENT_ID_KEY, shrProperties.getIdPClientId());
        httpHeaders.add(AUTH_TOKEN_KEY, shrProperties.getIdPAuthToken());
        return httpHeaders;
    }

    public static HttpHeaders getIdentityHeaders(HttpServletRequest request) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(CLIENT_ID_KEY, request.getHeader(CLIENT_ID_KEY));
        httpHeaders.add(FROM_KEY, request.getHeader(FROM_KEY));
        httpHeaders.add(AUTH_TOKEN_KEY, request.getHeader(AUTH_TOKEN_KEY));
        return httpHeaders;
    }
}
