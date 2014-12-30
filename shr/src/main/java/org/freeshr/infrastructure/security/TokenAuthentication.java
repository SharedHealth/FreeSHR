package org.freeshr.infrastructure.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;

public class TokenAuthentication implements Authentication {
    private String name;
    private String token;
    private List<? extends GrantedAuthority> roles;

    public TokenAuthentication(String name, String token, List<? extends GrantedAuthority> roles) {
        this.name = name;
        this.token = token;
        this.roles = roles;
    }

    public String getName() {
        return name;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles;
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getDetails() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return token;
    }

    @Override
    public boolean isAuthenticated() {
        return true;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TokenAuthentication tokenAuthentication = (TokenAuthentication) o;

        if (!token.equals(tokenAuthentication.token)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return token.hashCode();
    }

    @Override
    public String toString() {
        return "UserInfo{" +
                "name='" + name + '\'' +
                ", token='" + token + '\'' +
                ", roles=" + roles +
                '}';
    }
}
