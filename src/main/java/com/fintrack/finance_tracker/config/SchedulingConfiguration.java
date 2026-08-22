package com.fintrack.finance_tracker.config;

import com.fintrack.finance_tracker.health.DatabaseHealthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
public class SchedulingConfiguration {

    private static final Logger log = LoggerFactory.getLogger(SchedulingConfiguration.class);

    private final DatabaseHealthService databaseHealthService;

    public SchedulingConfiguration(DatabaseHealthService databaseHealthService) {
        this.databaseHealthService = databaseHealthService;
    }

    /**
     * Internal backup ping that reuses the same SELECT 1 logic as GET /health/db.
     * Fires every 3 days (259200000 ms).
     * NOTE: This only fires reliably while the app itself is being kept alive by an
     * external pinger. Render's free tier suspends the entire process on spin-down,
     * so this scheduler cannot wake the app on its own — an external cron (e.g.
     * cron-job.org / UptimeRobot / GitHub Actions cron) hitting /health every
     * 10-14 minutes is still required to prevent the instance from sleeping.
     */
    @Scheduled(fixedRate = 259200000)
    public void pingDatabasePeriodically() {
        try {
            databaseHealthService.pingDatabase();
            log.info("Scheduled database keep-alive ping succeeded (SELECT 1)");
        } catch (Exception ex) {
            log.warn("Scheduled database keep-alive ping failed", ex);
        }
    }
}
