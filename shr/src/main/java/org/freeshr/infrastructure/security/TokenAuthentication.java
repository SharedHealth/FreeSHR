package org.freeshr.infrastructure.security;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;

import java.util.Collection;
import java.util.List;

public class TokenAuthentication implements Authentication {
    private String name;
    private String token;
    private List<? extends GrantedAuthority> roles;

    public TokenAuthentication(UserInfo userInfo, String token) {
        this.name = userInfo.getName();
        this.token= token;

        String commaSeparateRoles = StringUtils.join(userInfo.getRoles(), ",");
        this.roles = AuthorityUtils.commaSeparatedStringToAuthorityList
                (commaSeparateRoles);
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
        return "TokenAuthentication{" +
                "name='" + name + '\'' +
                ", token='" + token + '\'' +
                ", roles=" + roles +
                '}';
    }
}
