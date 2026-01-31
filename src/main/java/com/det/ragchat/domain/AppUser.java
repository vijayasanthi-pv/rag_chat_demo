package com.det.ragchat.domain;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Application user identity used for authorization (API-key mode).
 *
 * NOTE: This is intentionally simple for the case-study:
 * - userId is the stable identifier provided via header (or from JWT in JWT mode)
 * - roles are stored in DB and resolved at request time
 */
@Entity
@Table(name = "app_users")
public class AppUser {

    @Id
    @Column(nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true, length = 128)
    private String userId;

    @Column(name = "display_name", length = 200)
    private String displayName;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "app_user_roles", joinColumns = @JoinColumn(name = "app_user_id"))
    @Column(name = "role", nullable = false, length = 64)
    private Set<String> roles = new HashSet<>();

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (createdAt == null) createdAt = OffsetDateTime.now();
        if (updatedAt == null) updatedAt = createdAt;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Set<String> getRoles() { return roles; }
    public void setRoles(Set<String> roles) { this.roles = roles; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
