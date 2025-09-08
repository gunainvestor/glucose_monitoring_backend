package com.dexmon.notification.model;

import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("glucose_alerts")
public class GlucoseAlert {

    public enum AlertType { HIGH, LOW }
    public enum Status { SENT, ACKNOWLEDGED }

    @PrimaryKeyColumn(name = "user_id", type = PrimaryKeyType.PARTITIONED)
    private UUID userId;

    @PrimaryKeyColumn(name = "timestamp")
    private Instant timestamp;

    private UUID alertId;
    private int glucoseValue;
    private AlertType alertType;
    private Status status = Status.SENT;

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    public UUID getAlertId() { return alertId; }
    public void setAlertId(UUID alertId) { this.alertId = alertId; }
    public int getGlucoseValue() { return glucoseValue; }
    public void setGlucoseValue(int glucoseValue) { this.glucoseValue = glucoseValue; }
    public AlertType getAlertType() { return alertType; }
    public void setAlertType(AlertType alertType) { this.alertType = alertType; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
}





