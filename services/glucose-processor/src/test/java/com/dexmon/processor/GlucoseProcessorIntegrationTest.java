package com.dexmon.processor;

import com.dexmon.common.model.GlucoseEvent;
import com.dexmon.processor.model.GlucoseReading;
import com.dexmon.processor.repo.GlucoseReadingRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class GlucoseProcessorIntegrationTest {

    @Container
    static KafkaContainer kafka = new KafkaContainer("confluentinc/cp-kafka:7.5.0");

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("timescale/timescaledb:2.14.2-pg15")
            .withDatabaseName("dexmon")
            .withUsername("postgres")
            .withPassword("postgres");

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.datasource.url", () -> postgres.getJdbcUrl());
        registry.add("spring.datasource.username", () -> postgres.getUsername());
        registry.add("spring.datasource.password", () -> postgres.getPassword());
    }

    @Autowired
    KafkaTemplate<String, GlucoseEvent> kafkaTemplate;

    @Autowired
    GlucoseReadingRepository repository;

    @Test
    void processesSensorEventAndWritesToDb() throws Exception {
        UUID sensorId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Instant ts = Instant.now();
        GlucoseEvent event = new GlucoseEvent();
        event.setEventId(UUID.randomUUID());
        event.setSensorId(sensorId);
        event.setUserId(userId);
        event.setTimestamp(ts);
        event.setGlucoseValue(110);
        event.setSource(GlucoseEvent.Source.SENSOR);

        kafkaTemplate.send("glucose.events", sensorId.toString(), event).get();

        Instant deadline = Instant.now().plus(Duration.ofSeconds(20));
        Optional<GlucoseReading> found;
        do {
            Thread.sleep(500);
            found = repository.findBySensorIdAndTimestamp(sensorId, ts);
        } while (found.isEmpty() && Instant.now().isBefore(deadline));

        assertThat(found).isPresent();
        assertThat(found.get().getGlucoseValue()).isEqualTo(110);
    }
}




