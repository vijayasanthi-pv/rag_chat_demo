package com.example.ragchat.service;

import com.example.ragchat.domain.AppUser;
import com.example.ragchat.repo.AppUserRepository;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;

@Service
public class AppUserService {

    private final AppUserRepository repo;

    public AppUserService(AppUserRepository repo) {
        this.repo = repo;
    }

    public AppUser getActiveByUserIdOrNull(String userId) {
        return repo.findByUserId(userId)
                .filter(AppUser::isActive)
                .orElse(null);
    }

    public Set<String> rolesForUserId(String userId) {
        AppUser u = getActiveByUserIdOrNull(userId);
        if (u == null) return Collections.emptySet();
        return u.getRoles() == null ? Collections.emptySet() : u.getRoles();
    }
}
