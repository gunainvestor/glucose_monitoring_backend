# DexMon Glucose Monitoring Backend – Product-Level Architecture

## Overview
DexMon ingests CGM readings, processes them in real time with Kafka, stores hot data in TimescaleDB, persists alerts in Cassandra, and archives cold data to S3 for analytics.

## High-Level Architecture
(Diagram source: `docs/diagrams/architecture.mmd`)
```mermaid
flowchart LR
  M[Mobile App] -->|REST /api/v1/glucose| R[Glucose Receiver]
  R -->|produce| K[(Kafka\n(glucose.events))]
  K --> P[Glucose Processor]
  K --> N[Notification Service]
  P --> T[(TimescaleDB)]
  N --> C[(Cassandra)]
  A[Data Archival] -->|read >90d| T
  A -->|Parquet| S3[(S3)]
  G[API Gateway] -->|query readings| T
  G -->|query alerts| C
  P -.DLQ.-> KDLQ[(glucose.events.dlq)]
  N -.DLQ.-> ADLQ[(glucose.events.alerts.dlq)]
```

## Sequence (Ingestion → Alerting → Query)
```mermaid
sequenceDiagram
  autonumber
  participant App as Mobile App
  participant R as Receiver
  participant K as Kafka
  participant P as Processor
  participant TS as TimescaleDB
  participant N as Notification
  participant CS as Cassandra
  participant G as API Gateway

  App->>R: POST /api/v1/glucose/sensor|manual
  R->>K: Produce GlucoseEvent (key=sensor_id)
  K->>P: Consume event (ordered per sensor)
  P->>P: Dedup (sensor_id+timestamp), resolve conflicts
  P->>TS: Upsert reading (version, etag)
  K->>N: Consume event
  N->>N: Threshold check (<70 or >250)
  N->>CS: Persist alert (SENT)
  App->>G: GET /readings, /alerts
  G->>TS: Query readings
  G->>CS: Query alerts
```

## Data Stores
- TimescaleDB: table `glucose_readings(id, sensor_id, user_id, timestamp, glucose_value, source, version, etag, created_at, updated_at)`; hypertable on `timestamp`; unique `(sensor_id, timestamp)`.
- Cassandra: table `glucose_alerts(user_id, timestamp, alert_id, glucose_value, alert_type, status)` partitioned by `user_id`.
- S3: Parquet archives partitioned by date and/or user.

## Reliability & Semantics
- Producer: idempotent (`enable.idempotence=true`, `acks=all`) and key by `sensor_id` for ordering.
- Consumer: retries with DLQ; DB dedup enforces effectively-once writes.

## Conflict Resolution
- SENSOR outweighs MANUAL at same `(sensor_id, timestamp)`; bump `version`, recompute `etag`.

## Security & Observability
- OAuth2/JWT (dev override: `security.disabled=true`).
- Prometheus metrics via Actuator; Grafana dashboards.

## Deployment
- Local: docker-compose (Kafka, TimescaleDB, Cassandra, Prometheus, Grafana).
- K8s: manifests in `k8s/` (namespace `dexmon`).
- CI: GitHub Actions builds/tests.
