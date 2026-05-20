package org.nmgyj.authentication.task;

import org.nmgyj.authentication.config.LogArchiveProperties;
import org.nmgyj.authentication.service.LogArchiveService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 历史日志归档清理定时任务。
 *
 * @author nmgyj
 * @since 2026-05-18
 */
@Component
public class LogArchiveCleanupTask {

    private static final Logger log = LoggerFactory.getLogger(LogArchiveCleanupTask.class);

    private final LogArchiveProperties properties;
    private final LogArchiveService logArchiveService;

    public LogArchiveCleanupTask(LogArchiveProperties properties, LogArchiveService logArchiveService) {
        this.properties = properties;
        this.logArchiveService = logArchiveService;
    }

    @Scheduled(cron = "${sdp.logging.archive.cron:0 30 3 * * ?}")
    public void cleanupExpiredLogs() {
        if (!properties.isEnabled()) {
            return;
        }
        try {
            logArchiveService.cleanupExpiredLogs();
        } catch (Exception ex) {
            log.error("log archive cleanup failed", ex);
        }
    }
}
