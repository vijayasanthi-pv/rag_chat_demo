package com.example.ragchat.repo;

import com.example.ragchat.domain.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {
    Page<ChatMessage> findBySession_IdAndUserId(UUID sessionId, String userId, Pageable pageable);
    long deleteBySession_IdAndUserId(UUID sessionId, String userId);
}
