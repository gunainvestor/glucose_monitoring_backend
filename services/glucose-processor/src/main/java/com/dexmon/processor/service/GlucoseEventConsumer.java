package com.dexmon.processor.service;

import com.dexmon.common.model.GlucoseEvent;
import com.dexmon.processor.model.GlucoseReading;
import com.dexmon.processor.repo.GlucoseReadingRepository;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class GlucoseEventConsumer {

    private final GlucoseReadingRepository repository;

    public GlucoseEventConsumer(GlucoseReadingRepository repository) {
        this.repository = repository;
    }

    @KafkaListener(topics = "glucose.events", groupId = "glucose-processor")
    @Transactional
    public void onMessage(ConsumerRecord<String, GlucoseEvent> record) {
        GlucoseEvent event = record.value();
        UUID sensorId = event.getSensorId();
        Instant timestamp = event.getTimestamp();

        Optional<GlucoseReading> existingOpt = repository.findBySensorIdAndTimestamp(sensorId, timestamp);
        if (existingOpt.isPresent()) {
            GlucoseReading existing = existingOpt.get();
            boolean sensorWins = event.getSource() == GlucoseEvent.Source.SENSOR || existing.getSource() == GlucoseReading.Source.MANUAL;
            if (sensorWins) {
                existing.setGlucoseValue(event.getGlucoseValue());
                existing.setSource(GlucoseReading.Source.valueOf(event.getSource().name()));
                existing.setVersion(existing.getVersion() + 1);
                existing.setUpdatedAt(Instant.now());
                existing.setEtag(generateEtag(existing));
                repository.save(existing);
            }
            return;
        }

        GlucoseReading reading = new GlucoseReading();
        reading.setId(event.getEventId() != null ? event.getEventId() : UUID.randomUUID());
        reading.setSensorId(event.getSensorId());
        reading.setUserId(event.getUserId());
        reading.setTimestamp(event.getTimestamp());
        reading.setGlucoseValue(event.getGlucoseValue());
        reading.setSource(GlucoseReading.Source.valueOf(event.getSource().name()));
        reading.setVersion(event.getVersion() == 0 ? 1 : event.getVersion());
        reading.setCreatedAt(Instant.now());
        reading.setUpdatedAt(Instant.now());
        reading.setEtag(generateEtag(reading));
        repository.save(reading);
    }

    private String generateEtag(GlucoseReading reading) {
        String input = reading.getSensorId() + ":" + reading.getTimestamp() + ":" + reading.getGlucoseValue() + ":" + reading.getVersion();
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}


