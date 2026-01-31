package com.det.ragchat.service;

import com.det.ragchat.crypto.CryptoService;
import com.det.ragchat.domain.ChatSession;
import com.det.ragchat.repo.ChatSessionRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class ChatSessionService {

    private final ChatSessionRepository sessionRepo;
    private final CryptoService crypto;

    public ChatSessionService(ChatSessionRepository sessionRepo, CryptoService crypto) {
        this.sessionRepo = sessionRepo;
        this.crypto = crypto;
    }

    @Transactional
    public ChatSession create(String userId, String title) {
        ChatSession s = new ChatSession();
        s.setUserId(userId);
        String t = (title == null || title.isBlank()) ? "New Chat" : title.trim();
        s.setTitle(crypto.encryptIfEnabled(t));
        s.setFavorite(false);
        s.setDeletedAt(null);
        return sessionRepo.save(s);
    }

    @Transactional(readOnly = true)
    public Page<ChatSession> list(String userId, Optional<Boolean> favorite, Pageable pageable) {
        if (favorite.isPresent()) {
            return sessionRepo.findByUserIdAndFavoriteAndDeletedAtIsNull(userId, favorite.get(), pageable);
        }
        return sessionRepo.findByUserIdAndDeletedAtIsNull(userId, pageable);
    }

    @Transactional(readOnly = true)
    public ChatSession getOwned(UUID sessionId, String userId) {
        return sessionRepo.findByIdAndUserIdAndDeletedAtIsNull(sessionId, userId)
                .orElseThrow(() -> new NotFoundException("Session not found"));
    }

    @Transactional
    public ChatSession rename(UUID sessionId, String userId, String newTitle) {
        ChatSession s = getOwned(sessionId, userId);
        s.setTitle(crypto.encryptIfEnabled(newTitle.trim()));
        return sessionRepo.save(s);
    }

    @Transactional
    public ChatSession setFavorite(UUID sessionId, String userId, boolean favorite) {
        ChatSession s = getOwned(sessionId, userId);
        s.setFavorite(favorite);
        return sessionRepo.save(s);
    }

    /**
     * Soft delete: keep rows (and messages) until retention job hard deletes.
     */
    @Transactional
    public void softDelete(UUID sessionId, String userId) {
        ChatSession s = getOwned(sessionId, userId);
        s.setDeletedAt(OffsetDateTime.now());
        s.setUpdatedAt(OffsetDateTime.now());
        sessionRepo.save(s);
    }

    @Transactional
    public void hardDeleteById(UUID sessionId) {
        sessionRepo.deleteById(sessionId);
    }
}
