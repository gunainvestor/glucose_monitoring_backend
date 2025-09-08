package com.dexmon.processor.repo;

import com.dexmon.processor.model.GlucoseReading;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface GlucoseReadingRepository extends JpaRepository<GlucoseReading, UUID> {
    Optional<GlucoseReading> findBySensorIdAndTimestamp(UUID sensorId, Instant timestamp);
}





