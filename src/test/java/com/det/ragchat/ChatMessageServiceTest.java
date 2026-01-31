package com.det.ragchat;

import com.det.ragchat.crypto.CryptoService;
import com.det.ragchat.crypto.NoopCryptoService;
import com.det.ragchat.domain.ChatMessage;
import com.det.ragchat.domain.ChatSession;
import com.det.ragchat.domain.Sender;
import com.det.ragchat.repo.ChatMessageRepository;
import com.det.ragchat.service.ChatMessageService;
import com.det.ragchat.service.ChatSessionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChatMessageServiceTest {

    @Test
    void addMessage_saves_message_for_owned_session() throws Exception {
        ChatSessionService sessionService = mock(ChatSessionService.class);
        ChatMessageRepository repo = mock(ChatMessageRepository.class);

        CryptoService crypto = new NoopCryptoService();
        ChatMessageService svc = new ChatMessageService(sessionService, repo, crypto);

        UUID sessionId = UUID.randomUUID();
        ChatSession s = new ChatSession();
        s.setId(sessionId);
        s.setUserId("u1");
        s.setTitle("Chat");

        when(sessionService.getOwned(sessionId, "u1")).thenReturn(s);
        when(repo.save(any(ChatMessage.class))).thenAnswer(inv -> inv.getArgument(0));

        var ctx = new ObjectMapper().readTree("""
          {"docs":[{"id":"d1","score":0.91}]}
        """);

        ChatMessage m = svc.addMessage(sessionId, "u1", Sender.USER, "hello", ctx);

        assertThat(m.getSession().getId()).isEqualTo(sessionId);
        assertThat(m.getUserId()).isEqualTo("u1");
        assertThat(m.getSender()).isEqualTo(Sender.USER);
        assertThat(m.getContent()).isEqualTo("hello");
        assertThat(m.getContext()).isNotNull();

        verify(sessionService).getOwned(sessionId, "u1");
        verify(repo).save(any(ChatMessage.class));
    }
}
