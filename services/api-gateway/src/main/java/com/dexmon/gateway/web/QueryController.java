package com.dexmon.gateway.web;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.cassandra.core.CassandraTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class QueryController {

    private final JdbcTemplate jdbc;
    private final CassandraTemplate cassandra;

    public QueryController(JdbcTemplate jdbc, CassandraTemplate cassandra) {
        this.jdbc = jdbc;
        this.cassandra = cassandra;
    }

    @GetMapping("/readings")
    public List<Map<String, Object>> getReadings(@RequestParam UUID userId,
                                                 @RequestParam Instant from,
                                                 @RequestParam Instant to) {
        return jdbc.queryForList(
                "select sensor_id, user_id, timestamp, glucose_value, source, version from glucose_readings where user_id = ? and timestamp between ? and ? order by timestamp",
                userId, from, to
        );
    }

    @GetMapping("/alerts")
    public List<Map<String, Object>> getAlerts(@RequestParam UUID userId,
                                               @RequestParam(required = false) Integer limit) {
        int lim = limit == null ? 50 : limit;
        return cassandra.getCqlOperations().queryForList(
                "select alert_id, user_id, timestamp, glucose_value, alert_type, status from dexmon_alerts.glucose_alerts where user_id = ? limit ?",
                userId, lim
        );
    }
}




