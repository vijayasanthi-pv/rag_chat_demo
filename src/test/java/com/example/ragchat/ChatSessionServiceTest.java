package com.example.ragchat;

import com.example.ragchat.crypto.CryptoService;
import com.example.ragchat.crypto.NoopCryptoService;
import com.example.ragchat.domain.ChatSession;
import com.example.ragchat.repo.ChatSessionRepository;
import com.example.ragchat.service.ChatSessionService;
import com.example.ragchat.service.NotFoundException;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChatSessionServiceTest {

    @Test
    void rename_updates_title_for_owned_session() {
        ChatSessionRepository repo = mock(ChatSessionRepository.class);
        CryptoService crypto = new NoopCryptoService();
        ChatSessionService service = new ChatSessionService(repo, crypto);

        UUID sessionId = UUID.randomUUID();
        ChatSession s = new ChatSession();
        s.setId(sessionId);
        s.setUserId("u1");
        s.setTitle("Old");

        when(repo.findByIdAndUserIdAndDeletedAtIsNull(sessionId, "u1")).thenReturn(Optional.of(s));
        when(repo.save(any(ChatSession.class))).thenAnswer(inv -> inv.getArgument(0));

        ChatSession updated = service.rename(sessionId, "u1", "New Title");

        assertThat(updated.getTitle()).isEqualTo("New Title");

        ArgumentCaptor<ChatSession> captor = ArgumentCaptor.forClass(ChatSession.class);
        verify(repo).save(captor.capture());
        assertThat(captor.getValue().getTitle()).isEqualTo("New Title");
    }

    @Test
    void getOwned_throws_when_not_found() {
        ChatSessionRepository repo = mock(ChatSessionRepository.class);
        CryptoService crypto = new NoopCryptoService();
        ChatSessionService service = new ChatSessionService(repo, crypto);

        UUID sessionId = UUID.randomUUID();
        when(repo.findByIdAndUserIdAndDeletedAtIsNull(sessionId, "u1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getOwned(sessionId, "u1"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Session not found");
    }
}
