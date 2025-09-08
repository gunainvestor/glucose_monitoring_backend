package com.dexmon.simulator;

import com.dexmon.common.model.GlucoseEvent;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Instant;
import java.util.Random;
import java.util.UUID;

@SpringBootApplication
public class SimulatorApplication implements CommandLineRunner {

    private final KafkaTemplate<String, GlucoseEvent> kafkaTemplate;

    public SimulatorApplication(KafkaTemplate<String, GlucoseEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public static void main(String[] args) {
        SpringApplication.run(SimulatorApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        Random rnd = new Random();
        UUID sensorId = System.getenv("SIM_SENSOR_ID") != null ? UUID.fromString(System.getenv("SIM_SENSOR_ID")) : UUID.randomUUID();
        UUID userId = System.getenv("SIM_USER_ID") != null ? UUID.fromString(System.getenv("SIM_USER_ID")) : UUID.randomUUID();
        for (int i = 0; i < 100; i++) {
            GlucoseEvent event = new GlucoseEvent();
            event.setEventId(UUID.randomUUID());
            event.setSensorId(sensorId);
            event.setUserId(userId);
            event.setTimestamp(Instant.now());
            event.setGlucoseValue(70 + rnd.nextInt(200));
            event.setSource(GlucoseEvent.Source.SENSOR);
            kafkaTemplate.send("glucose.events", sensorId.toString(), event).get();
            Thread.sleep(200);
        }
    }
}


