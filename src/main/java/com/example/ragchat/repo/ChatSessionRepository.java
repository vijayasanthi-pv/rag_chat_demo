package com.example.ragchat.repo;

import com.example.ragchat.domain.ChatSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChatSessionRepository extends JpaRepository<ChatSession, UUID> {

    Optional<ChatSession> findByIdAndUserIdAndDeletedAtIsNull(UUID id, String userId);

    Page<ChatSession> findByUserIdAndDeletedAtIsNull(String userId, Pageable pageable);

    Page<ChatSession> findByUserIdAndFavoriteAndDeletedAtIsNull(String userId, boolean favorite, Pageable pageable);

    @Query("select s.id from ChatSession s where (s.deletedAt is not null and s.deletedAt < :softDeletedBefore) or (s.deletedAt is null and s.updatedAt < :inactiveBefore)")
    List<UUID> findIdsForRetentionPurge(OffsetDateTime softDeletedBefore, OffsetDateTime inactiveBefore);
}
