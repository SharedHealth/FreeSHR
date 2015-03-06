package org.freeshr.infrastructure.security;

public class UserAuthInfo {
    private String clientId;
    private String email;

    public UserAuthInfo(String clientId, String email) {
        this.clientId = clientId;
        this.email = email;
    }

    public String getClientId() {
        return clientId;
    }

    public String getEmail() {
        return email;
    }
}
