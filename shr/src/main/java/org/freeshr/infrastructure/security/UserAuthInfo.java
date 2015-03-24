package org.freeshr.infrastructure.security;

public class UserAuthInfo {
    private String clientId;
    private String email;
    private String token;

    public UserAuthInfo(String clientId, String email, String token) {
        this.clientId = clientId;
        this.email = email;
        this.token = token;
    }

    public String getClientId() {
        return clientId;
    }

    public String getEmail() {
        return email;
    }

    public String getToken() {
        return token;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserAuthInfo that = (UserAuthInfo) o;

        if (!clientId.equals(that.clientId)) return false;
        if (!email.equals(that.email)) return false;
        if (!token.equals(that.token)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = clientId.hashCode();
        result = 31 * result + email.hashCode();
        result = 31 * result + token.hashCode();
        return result;
    }
}
