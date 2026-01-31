package com.det.ragchat.service;

import com.det.ragchat.crypto.CryptoService;
import com.det.ragchat.domain.ChatMessage;
import com.det.ragchat.domain.ChatSession;
import com.det.ragchat.domain.Sender;
import com.det.ragchat.repo.ChatMessageRepository;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ChatMessageService {

    private final ChatSessionService sessionService;
    private final ChatMessageRepository msgRepo;
    private final CryptoService crypto;

    public ChatMessageService(ChatSessionService sessionService, ChatMessageRepository msgRepo, CryptoService crypto) {
        this.sessionService = sessionService;
        this.msgRepo = msgRepo;
        this.crypto = crypto;
    }

    @Transactional
    public ChatMessage addMessage(UUID sessionId, String userId, Sender sender, String content, JsonNode context) {
        ChatSession session = sessionService.getOwned(sessionId, userId);

        ChatMessage m = new ChatMessage();
        m.setSession(session);
        m.setUserId(userId);
        m.setSender(sender);
        m.setContent(crypto.encryptIfEnabled(content));
        m.setContext(crypto.encryptContextIfEnabled(context));

        return msgRepo.save(m);
    }

    @Transactional(readOnly = true)
    public Page<ChatMessage> listMessages(UUID sessionId, String userId, Pageable pageable) {
        sessionService.getOwned(sessionId, userId);
        return msgRepo.findBySession_IdAndUserId(sessionId, userId, pageable);
    }
}
