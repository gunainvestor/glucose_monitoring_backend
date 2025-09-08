CREATE EXTENSION IF NOT EXISTS timescaledb;

CREATE TABLE IF NOT EXISTS glucose_readings (
    id UUID PRIMARY KEY,
    sensor_id UUID NOT NULL,
    user_id UUID NOT NULL,
    timestamp TIMESTAMPTZ NOT NULL,
    glucose_value INT NOT NULL,
    source TEXT NOT NULL,
    version INT NOT NULL DEFAULT 1,
    etag TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

SELECT create_hypertable('glucose_readings', 'timestamp', if_not_exists => TRUE);

CREATE UNIQUE INDEX IF NOT EXISTS idx_sensor_timestamp ON glucose_readings(sensor_id, timestamp);
CREATE INDEX IF NOT EXISTS idx_user_timestamp ON glucose_readings(user_id, timestamp DESC);



