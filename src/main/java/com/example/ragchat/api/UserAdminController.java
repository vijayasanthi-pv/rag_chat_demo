package com.example.ragchat.api;

import com.example.ragchat.api.dto.CreateUserRequest;
import com.example.ragchat.api.dto.SetUserRolesRequest;
import com.example.ragchat.api.dto.UserResponse;
import com.example.ragchat.domain.AppUser;
import com.example.ragchat.service.AppUserAdminService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/users")
public class UserAdminController {

    private final AppUserAdminService svc;

    public UserAdminController(AppUserAdminService svc) {
        this.svc = svc;
    }

    @Operation(summary = "List users (admin)")
    @PreAuthorize("hasAnyAuthority('ROLE_CHAT_ADMIN')")
    @GetMapping
    public List<UserResponse> list() {
        return svc.list().stream().map(this::toDto).toList();
    }

    @Operation(summary = "Create/update a user and roles (admin)")
    @PreAuthorize("hasAnyAuthority('ROLE_CHAT_ADMIN')")
    @PostMapping
    public UserResponse create(@Valid @RequestBody CreateUserRequest req) {
        AppUser u = svc.createOrUpdate(req.userId(), req.displayName(), req.roles(), req.active());
        return toDto(u);
    }

    @Operation(summary = "Set roles for a user (admin)")
    @PreAuthorize("hasAnyAuthority('ROLE_CHAT_ADMIN')")
    @PutMapping("/{userId}/roles")
    public UserResponse setRoles(@PathVariable String userId, @Valid @RequestBody SetUserRolesRequest req) {
        return toDto(svc.setRoles(userId, req.roles()));
    }

    private UserResponse toDto(AppUser u) {
        return new UserResponse(u.getId(), u.getUserId(), u.getDisplayName(), u.isActive(), u.getRoles(), u.getCreatedAt(), u.getUpdatedAt());
    }
}
