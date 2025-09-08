package com.dexmon.notification.service;

import com.dexmon.common.model.GlucoseEvent;
import com.dexmon.notification.model.GlucoseAlert;
import com.dexmon.notification.repo.GlucoseAlertRepository;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class ThresholdNotificationConsumer {

    private final GlucoseAlertRepository repository;

    public ThresholdNotificationConsumer(GlucoseAlertRepository repository) {
        this.repository = repository;
    }

    @KafkaListener(topics = "glucose.events", groupId = "notification-service")
    public void onMessage(ConsumerRecord<String, GlucoseEvent> record) {
        GlucoseEvent event = record.value();
        int value = event.getGlucoseValue();
        boolean high = value > 250;
        boolean low = value < 70;
        if (!(high || low)) {
            return;
        }
        GlucoseAlert alert = new GlucoseAlert();
        alert.setAlertId(UUID.randomUUID());
        alert.setUserId(event.getUserId());
        alert.setTimestamp(Instant.now());
        alert.setGlucoseValue(value);
        alert.setAlertType(high ? GlucoseAlert.AlertType.HIGH : GlucoseAlert.AlertType.LOW);
        repository.save(alert);
        // TODO: integrate with push/SMS/email providers
    }
}





