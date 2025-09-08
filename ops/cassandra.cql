CREATE KEYSPACE IF NOT EXISTS dexmon_alerts WITH REPLICATION = {'class': 'SimpleStrategy', 'replication_factor': 1};

USE dexmon_alerts;

CREATE TABLE IF NOT EXISTS glucose_alerts (
    user_id uuid,
    timestamp timestamp,
    alert_id uuid,
    glucose_value int,
    alert_type text,
    status text,
    PRIMARY KEY ((user_id), timestamp)
) WITH CLUSTERING ORDER BY (timestamp DESC);




