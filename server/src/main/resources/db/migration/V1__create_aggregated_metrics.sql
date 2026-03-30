CREATE TABLE IF NOT EXISTS aggregated_metrics (
    id VARCHAR(36) PRIMARY KEY,
    agent_id VARCHAR(36) NOT NULL,
    metric_name VARCHAR(255) NOT NULL,
    avg_value DOUBLE NOT NULL,
    min_value DOUBLE NOT NULL,
    max_value DOUBLE NOT NULL,
    count INTEGER NOT NULL,
    timestamp_bucket BIGINT NOT NULL,
    created_at BIGINT NOT NULL,
    CONSTRAINT fk_aggregated_metrics_agent FOREIGN KEY (agent_id)
        REFERENCES agents(id)
);
