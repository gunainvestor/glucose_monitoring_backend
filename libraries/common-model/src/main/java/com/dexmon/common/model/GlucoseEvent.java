package com.dexmon.common.model;

import java.time.Instant;
import java.util.UUID;

public class GlucoseEvent {
    public enum Source { SENSOR, MANUAL }

    private UUID eventId;
    private UUID sensorId;
    private UUID userId;
    private Instant timestamp;
    private int glucoseValue;
    private Source source;
    private int version;
    private String etag;

    public UUID getEventId() { return eventId; }
    public void setEventId(UUID eventId) { this.eventId = eventId; }
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
}





