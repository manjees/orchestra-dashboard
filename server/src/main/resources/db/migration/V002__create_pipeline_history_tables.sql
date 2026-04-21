CREATE TABLE IF NOT EXISTS pipeline_history (
    id VARCHAR(36) PRIMARY KEY,
    project_name VARCHAR(255) NOT NULL,
    issue_num INT NOT NULL,
    issue_title VARCHAR(500) NOT NULL,
    mode VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'RUNNING',
    started_at BIGINT NOT NULL,
    completed_at BIGINT,
    elapsed_total_sec DOUBLE PRECISION DEFAULT 0.0,
    pr_url VARCHAR(500),
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_pipeline_history_project ON pipeline_history (project_name);
CREATE INDEX idx_pipeline_history_status ON pipeline_history (status);
CREATE INDEX idx_pipeline_history_started ON pipeline_history (started_at);
CREATE INDEX idx_pipeline_history_project_started ON pipeline_history (project_name, started_at);

CREATE TABLE IF NOT EXISTS pipeline_step_history (
    id VARCHAR(36) PRIMARY KEY,
    pipeline_history_id VARCHAR(36) NOT NULL,
    step_name VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    elapsed_sec DOUBLE PRECISION DEFAULT 0.0,
    fail_detail TEXT,
    CONSTRAINT fk_step_pipeline_history FOREIGN KEY (pipeline_history_id) REFERENCES pipeline_history (id) ON DELETE CASCADE
);

CREATE INDEX idx_step_history_pipeline ON pipeline_step_history (pipeline_history_id);
