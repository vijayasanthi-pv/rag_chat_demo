package com.example.ragchat.api;

import com.example.ragchat.api.dto.*;
import com.example.ragchat.crypto.CryptoService;
import com.example.ragchat.domain.ChatSession;
import com.example.ragchat.security.SecurityUtils;
import com.example.ragchat.service.ChatSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sessions")
public class ChatSessionController {

    private final ChatSessionService sessionService;
    private final CryptoService crypto;

    public ChatSessionController(ChatSessionService sessionService, CryptoService crypto) {
        this.sessionService = sessionService;
        this.crypto = crypto;
    }

    @Operation(summary = "Create a chat session")
    @PreAuthorize("hasAnyAuthority('ROLE_API','ROLE_CHAT_ADMIN','ROLE_CHAT_WRITE')")
    @PostMapping
    public SessionResponse create(@Valid @RequestBody(required = false) CreateSessionRequest req) {
        String userId = SecurityUtils.currentUserId();
        ChatSession s = sessionService.create(userId, req == null ? null : req.title());
        return toDto(s);
    }

    @Operation(summary = "List chat sessions for current user (optional favorite filter)")
    @PreAuthorize("hasAnyAuthority('ROLE_API','ROLE_CHAT_ADMIN','ROLE_CHAT_READ')")
    @GetMapping
    public PageResponse<SessionResponse> list(
            @RequestParam Optional<Boolean> favorite,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        String userId = SecurityUtils.currentUserId();
        Page<ChatSession> result = sessionService.list(
                userId,
                favorite,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"))
        );

        return new PageResponse<>(
                result.getContent().stream().map(this::toDto).toList(),
                new PageMeta(result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages())
        );
    }

    @Operation(summary = "Rename a chat session")
    @PreAuthorize("hasAnyAuthority('ROLE_API','ROLE_CHAT_ADMIN','ROLE_CHAT_WRITE')")
    @PatchMapping("/{sessionId}/rename")
    public SessionResponse rename(
            @Parameter(description = "Session UUID") @PathVariable UUID sessionId,
            @Valid @RequestBody RenameSessionRequest req
    ) {
        String userId = SecurityUtils.currentUserId();
        return toDto(sessionService.rename(sessionId, userId, req.title()));
    }

    @Operation(summary = "Mark/unmark a chat session as favorite")
    @PreAuthorize("hasAnyAuthority('ROLE_API','ROLE_CHAT_ADMIN','ROLE_CHAT_WRITE')")
    @PatchMapping("/{sessionId}/favorite")
    public SessionResponse favorite(
            @PathVariable UUID sessionId,
            @Valid @RequestBody FavoriteSessionRequest req
    ) {
        String userId = SecurityUtils.currentUserId();
        return toDto(sessionService.setFavorite(sessionId, userId, req.favorite()));
    }

    @Operation(summary = "Soft-delete a chat session (messages retained until retention job hard-deletes)")
    @PreAuthorize("hasAnyAuthority('ROLE_API','ROLE_CHAT_ADMIN','ROLE_CHAT_WRITE')")
    @DeleteMapping("/{sessionId}")
    public void delete(@PathVariable UUID sessionId) {
        String userId = SecurityUtils.currentUserId();
        sessionService.softDelete(sessionId, userId);
    }

    private SessionResponse toDto(ChatSession s) {
        String title = crypto.decryptIfEncrypted(s.getTitle());
        return new SessionResponse(s.getId(), title, s.isFavorite(), s.getCreatedAt(), s.getUpdatedAt());
    }
}
