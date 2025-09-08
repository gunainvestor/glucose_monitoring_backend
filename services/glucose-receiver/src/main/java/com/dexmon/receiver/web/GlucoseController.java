package com.dexmon.receiver.web;

import com.dexmon.common.model.GlucoseEvent;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/glucose")
public class GlucoseController {

    private final KafkaTemplate<String, GlucoseEvent> kafkaTemplate;

    public GlucoseController(KafkaTemplate<String, GlucoseEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @PostMapping("/sensor")
    public ResponseEntity<Void> ingestSensor(@Validated @RequestBody GlucoseEvent event) {
        if (event.getEventId() == null) {
            event.setEventId(UUID.randomUUID());
        }
        if (event.getTimestamp() == null) {
            event.setTimestamp(Instant.now());
        }
        event.setSource(GlucoseEvent.Source.SENSOR);
        kafkaTemplate.send("glucose.events", event.getSensorId().toString(), event);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/manual")
    public ResponseEntity<Void> ingestManual(@Validated @RequestBody GlucoseEvent event) {
        if (event.getEventId() == null) {
            event.setEventId(UUID.randomUUID());
        }
        if (event.getTimestamp() == null) {
            event.setTimestamp(Instant.now());
        }
        event.setSource(GlucoseEvent.Source.MANUAL);
        kafkaTemplate.send("glucose.events", event.getSensorId().toString(), event);
        return ResponseEntity.accepted().build();
    }
}





