package com.fintrack.finance_tracker.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class DatabaseHealthService {

    private static final Logger log = LoggerFactory.getLogger(DatabaseHealthService.class);

    private final JdbcTemplate jdbcTemplate;

    public DatabaseHealthService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Executes a trivial SELECT 1 query to keep the Supabase Postgres
     * connection / database active and to verify connectivity.
     *
     * @return true if the query succeeded
     */
    public boolean pingDatabase() {
        Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
        boolean ok = result != null && result == 1;
        if (ok) {
            log.debug("Database keep-alive ping succeeded (SELECT 1)");
        } else {
            log.warn("Database keep-alive ping returned unexpected result: {}", result);
        }
        return ok;
    }
}
