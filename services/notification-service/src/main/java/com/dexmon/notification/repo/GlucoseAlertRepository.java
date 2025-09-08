package com.dexmon.notification.repo;

import com.dexmon.notification.model.GlucoseAlert;
import org.springframework.data.cassandra.repository.CassandraRepository;

import java.util.UUID;

public interface GlucoseAlertRepository extends CassandraRepository<GlucoseAlert, UUID> {
}





