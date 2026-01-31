package com.det.ragchat.service;

import com.det.ragchat.config.AppProperties;
import com.det.ragchat.repo.ChatSessionRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Component
public class RetentionPurgeJob {

    private static final Logger log = LoggerFactory.getLogger(RetentionPurgeJob.class);

    private final AppProperties props;
    private final ChatSessionRepository sessionRepo;

    public RetentionPurgeJob(AppProperties props, ChatSessionRepository sessionRepo) {
        this.props = props;
        this.sessionRepo = sessionRepo;
    }

    @Scheduled(cron = "${app.retention.cron}")
    @Transactional
    public void purge() {
        if (!props.getRetention().isEnabled()) return;

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime softDeletedBefore = now.minusDays(props.getRetention().getSoftDeleteGraceDays());
        OffsetDateTime inactiveBefore = now.minusDays(props.getRetention().getHardDeleteAfterDays());

        List<UUID> ids = sessionRepo.findIdsForRetentionPurge(softDeletedBefore, inactiveBefore);
        if (ids.isEmpty()) return;

        log.info("Retention purge deleting {} sessions (softDeletedBefore={}, inactiveBefore={})",
                ids.size(), softDeletedBefore, inactiveBefore);

        sessionRepo.deleteAllByIdInBatch(ids); // messages removed via ON DELETE CASCADE
    }
}
