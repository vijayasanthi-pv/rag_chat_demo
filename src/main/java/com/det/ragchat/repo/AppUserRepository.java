package com.det.ragchat.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.det.ragchat.domain.AppUser;

import java.util.Optional;
import java.util.UUID;

public interface AppUserRepository extends JpaRepository<AppUser, UUID> {
    Optional<AppUser> findByUserId(String userId);
}
