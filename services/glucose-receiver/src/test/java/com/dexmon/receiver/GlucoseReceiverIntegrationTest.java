package com.dexmon.receiver;

import com.dexmon.common.model.GlucoseEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class GlucoseReceiverIntegrationTest {

    @Container
    static KafkaContainer kafka = new KafkaContainer("confluentinc/cp-kafka:7.5.0");

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate rest;

    @Test
    void postsSensorEvent() {
        GlucoseEvent event = new GlucoseEvent();
        event.setEventId(UUID.randomUUID());
        event.setSensorId(UUID.randomUUID());
        event.setUserId(UUID.randomUUID());
        event.setTimestamp(Instant.now());
        event.setGlucoseValue(123);
        ResponseEntity<Void> resp = rest.postForEntity("http://localhost:" + port + "/api/v1/glucose/sensor", event, Void.class);
        assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
    }
}




