package org.freeshr.utils;


import org.apache.commons.codec.binary.Base64;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;

import java.nio.charset.Charset;
import java.util.Arrays;

public class HttpUtil {

    public static MultiValueMap<String, String> basicAuthHeaders(String userName, String password) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        String auth = userName + ":" + password;
        headers.add("Authorization", "Basic " + new String(Base64.encodeBase64(auth.getBytes(Charset.forName("UTF-8")))));
        return headers;
    }

    public static MultiValueMap<String, String> basicHeaders( String password) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        String auth =   password;
        headers.add("X-Auth-Token",  auth);
        return headers;
    }


}
