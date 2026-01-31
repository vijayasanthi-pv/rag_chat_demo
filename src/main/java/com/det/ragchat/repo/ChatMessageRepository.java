package com.det.ragchat.repo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.det.ragchat.domain.ChatMessage;

import java.util.UUID;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {
    Page<ChatMessage> findBySession_IdAndUserId(UUID sessionId, String userId, Pageable pageable);
    long deleteBySession_IdAndUserId(UUID sessionId, String userId);
}
