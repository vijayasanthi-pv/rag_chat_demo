package com.example.ragchat.service;

import com.example.ragchat.domain.AppUser;
import com.example.ragchat.repo.AppUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
public class AppUserAdminService {

    private final AppUserRepository repo;

    public AppUserAdminService(AppUserRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public AppUser createOrUpdate(String userId, String displayName, Set<String> roles, Boolean active) {
        AppUser u = repo.findByUserId(userId).orElseGet(() -> {
            AppUser nu = new AppUser();
            nu.setUserId(userId);
            return nu;
        });

        if (displayName != null) u.setDisplayName(displayName);
        if (active != null) u.setActive(active);
        if (roles != null) {
            u.getRoles().clear();
            u.getRoles().addAll(roles);
        }

        return repo.save(u);
    }

    @Transactional(readOnly = true)
    public List<AppUser> list() {
        return repo.findAll();
    }

    @Transactional
    public AppUser setRoles(String userId, Set<String> roles) {
        AppUser u = repo.findByUserId(userId).orElseThrow(() -> new NotFoundException("User not found: " + userId));
        u.getRoles().clear();
        u.getRoles().addAll(roles);
        return repo.save(u);
    }
}
