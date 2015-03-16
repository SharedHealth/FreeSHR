package org.freeshr.domain.service;

import org.freeshr.infrastructure.security.UserInfo;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    public UserInfo getUserInfo() {
        return (UserInfo) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
