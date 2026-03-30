CREATE TABLE IF NOT EXISTS metrics (
    id VARCHAR(36) PRIMARY KEY,
    agent_id VARCHAR(36) NOT NULL,
    name VARCHAR(100) NOT NULL,
    value DOUBLE PRECISION NOT NULL,
    unit VARCHAR(50) NOT NULL DEFAULT '',
    timestamp BIGINT NOT NULL
);

CREATE INDEX idx_metrics_agent_timestamp ON metrics (agent_id, timestamp);
CREATE INDEX idx_metrics_agent_name_timestamp ON metrics (agent_id, name, timestamp);

CREATE TABLE IF NOT EXISTS metrics_aggregates (
    id VARCHAR(36) PRIMARY KEY,
    agent_id VARCHAR(36) NOT NULL,
    metric_name VARCHAR(100) NOT NULL,
    avg_value DOUBLE PRECISION NOT NULL,
    min_value DOUBLE PRECISION NOT NULL,
    max_value DOUBLE PRECISION NOT NULL,
    sample_count INT NOT NULL,
    window_start BIGINT NOT NULL,
    window_end BIGINT NOT NULL,
    created_at BIGINT NOT NULL
);

CREATE INDEX idx_aggregates_agent_metric_window
    ON metrics_aggregates (agent_id, metric_name, window_start, window_end);
