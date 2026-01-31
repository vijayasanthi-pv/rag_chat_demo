package com.det.ragchat.api;

import com.det.ragchat.api.dto.*;
import com.det.ragchat.crypto.CryptoService;
import com.det.ragchat.domain.ChatMessage;
import com.det.ragchat.security.SecurityUtils;
import com.det.ragchat.service.ChatMessageService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sessions/{sessionId}/messages")
public class ChatMessageController {

    private final ChatMessageService msgService;
    private final CryptoService crypto;

    public ChatMessageController(ChatMessageService msgService, CryptoService crypto) {
        this.msgService = msgService;
        this.crypto = crypto;
    }

    @Operation(summary = "Add a message to a session (supports optional RAG context JSON)")
    @PreAuthorize("hasAnyAuthority('ROLE_API','ROLE_CHAT_ADMIN','ROLE_CHAT_WRITE')")
    @PostMapping
    public MessageResponse add(
            @PathVariable UUID sessionId,
            @Valid @RequestBody CreateMessageRequest req
    ) {
        String userId = SecurityUtils.currentUserId();
        ChatMessage m = msgService.addMessage(sessionId, userId, req.sender(), req.content(), req.context());
        return toDto(m);
    }

    @Operation(summary = "Get message history of a session (paginated)")
    @PreAuthorize("hasAnyAuthority('ROLE_API','ROLE_CHAT_ADMIN','ROLE_CHAT_READ')")
    @GetMapping
    public PageResponse<MessageResponse> list(
            @PathVariable UUID sessionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "asc") String order
    ) {
        String userId = SecurityUtils.currentUserId();
        Sort sort = Sort.by("createdAt");
        sort = "desc".equalsIgnoreCase(order) ? sort.descending() : sort.ascending();

        Page<ChatMessage> result = msgService.listMessages(sessionId, userId, PageRequest.of(page, size, sort));

        return new PageResponse<>(
                result.getContent().stream().map(this::toDto).toList(),
                new PageMeta(result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages())
        );
    }

    private MessageResponse toDto(ChatMessage m) {
        return new MessageResponse(
                m.getId(),
                m.getSession().getId(),
                m.getSender(),
                crypto.decryptIfEncrypted(m.getContent()),
                crypto.decryptContextIfEncrypted(m.getContext()),
                m.getCreatedAt()
        );
    }
}
