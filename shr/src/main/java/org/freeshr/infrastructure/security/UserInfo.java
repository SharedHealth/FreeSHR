package org.freeshr.infrastructure.security;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserInfo {
    @JsonProperty("user")
    private String user;
    @JsonProperty("roles")
    private HashSet<String> roles;

    @JsonProperty("id")
    private String id;
    @JsonProperty("name")
    private String name;
    @JsonProperty("email")
    private String email;
    @JsonProperty("is_active")
    private int isActive;
    @JsonProperty("activated")
    private boolean activated;
    @JsonProperty("access_token")
    private String accessToken;
    @JsonProperty("groups")
    private List<String> groups;
    @JsonProperty("profiles")
    private List<Object> profiles;

    public UserInfo() {
    }

    public UserInfo(String user, List<String> roles) {
        this.user = user;
        this.roles = new HashSet<>(roles);
    }

    public UserInfo(String id, String name, String email, int isActive, boolean activated, String accessToken, List<String> groups, List<Object> profiles) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.isActive = isActive;
        this.activated = activated;
        this.accessToken = accessToken;
        this.groups = groups;
        this.profiles = profiles;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public List<String> getRoles() {
        return new ArrayList<>(roles);
    }

    public String getName() {
        return name;
    }

    public List<String> getGroups() {
        return groups;
    }

    public String getId() {
        return id;
    }

    public List<Object> getProfiles() {
        return profiles;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public boolean isActivated() {
        return activated;
    }

    public int getIsActive() {
        return isActive;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserInfo userInfo = (UserInfo) o;

        if (activated != userInfo.activated) return false;
        if (isActive != userInfo.isActive) return false;
        if (!accessToken.equals(userInfo.accessToken)) return false;
        if (!email.equals(userInfo.email)) return false;
        if (groups != null ? !groups.equals(userInfo.groups) : userInfo.groups != null) return false;
        if (!id.equals(userInfo.id)) return false;
        if (name != null ? !name.equals(userInfo.name) : userInfo.name != null) return false;
        if (profiles != null ? !profiles.equals(userInfo.profiles) : userInfo.profiles != null) return false;
        if (roles != null ? !roles.equals(userInfo.roles) : userInfo.roles != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + email.hashCode();
        result = 31 * result + isActive;
        result = 31 * result + (activated ? 1 : 0);
        result = 31 * result + accessToken.hashCode();
        result = 31 * result + (groups != null ? groups.hashCode() : 0);
        result = 31 * result + (profiles != null ? profiles.hashCode() : 0);
        return result;
    }
}
