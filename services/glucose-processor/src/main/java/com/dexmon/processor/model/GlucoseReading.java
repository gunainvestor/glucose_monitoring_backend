package com.dexmon.processor.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "glucose_readings")
public class GlucoseReading {

    public enum Source { SENSOR, MANUAL }

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "sensor_id", nullable = false)
    private UUID sensorId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    @Column(name = "glucose_value", nullable = false)
    private int glucoseValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false)
    private Source source;

    @Column(name = "version", nullable = false)
    private int version = 1;

    @Column(name = "etag", nullable = true)
    private String etag;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getSensorId() { return sensorId; }
    public void setSensorId(UUID sensorId) { this.sensorId = sensorId; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    public int getGlucoseValue() { return glucoseValue; }
    public void setGlucoseValue(int glucoseValue) { this.glucoseValue = glucoseValue; }
    public Source getSource() { return source; }
    public void setSource(Source source) { this.source = source; }
    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }
    public String getEtag() { return etag; }
    public void setEtag(String etag) { this.etag = etag; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}





