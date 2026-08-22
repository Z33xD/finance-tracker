package com.fintrack.finance_tracker.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HealthController {

    private static final Logger log = LoggerFactory.getLogger(HealthController.class);

    private final DatabaseHealthService databaseHealthService;

    public HealthController(DatabaseHealthService databaseHealthService) {
        this.databaseHealthService = databaseHealthService;
    }

    /**
     * Lightweight liveness probe — no database calls.
     * Intended to be hit every 10-14 minutes by an external pinger
     * to prevent Render free-tier spin-down (15 min idle threshold).
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    /**
     * Database keep-alive probe — runs SELECT 1 via JdbcTemplate
     * to keep the Supabase Postgres connection active.
     * Intended to be hit every 3-4 days externally, plus an internal
     * scheduled ping as a backup while the app is awake.
     */
    @GetMapping("/health/db")
    public ResponseEntity<Map<String, String>> dbHealth() {
        try {
            databaseHealthService.pingDatabase();
            return ResponseEntity.ok(Map.of("status", "db_ok"));
        } catch (Exception ex) {
            log.warn("Database health check failed", ex);
            return ResponseEntity.status(503).body(Map.of("status", "db_error", "error", ex.getMessage()));
        }
    }
}
