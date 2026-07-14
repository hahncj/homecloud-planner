-- Milestone 5: backup policies (the backup matrix) and architecture decision records.

CREATE TABLE backup_policy (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL REFERENCES project (id) ON DELETE RESTRICT,
    name VARCHAR(200) NOT NULL,
    data_category VARCHAR(200) NOT NULL,
    primary_location VARCHAR(200) NOT NULL,
    local_backup_location VARCHAR(200),
    offsite_backup_location VARCHAR(200),
    encrypted BOOLEAN NOT NULL DEFAULT FALSE,
    -- Not in the milestone's literal field list, but required to implement the
    -- explicitly-specified "missing encryption warning for sensitive off-site
    -- data" rule — see ADR-0005.
    contains_sensitive_data BOOLEAN NOT NULL DEFAULT FALSE,
    frequency VARCHAR(20) NOT NULL,
    retention VARCHAR(200),
    recovery_point_objective VARCHAR(100),
    recovery_time_objective VARCHAR(100),
    last_verified_date DATE,
    verification_notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT backup_policy_frequency_valid
        CHECK (frequency IN ('CONTINUOUS', 'HOURLY', 'DAILY', 'WEEKLY', 'MONTHLY', 'MANUAL'))
);

CREATE INDEX idx_backup_policy_project_id ON backup_policy (project_id);

CREATE TABLE architecture_decision (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL REFERENCES project (id) ON DELETE RESTRICT,
    title VARCHAR(200) NOT NULL,
    status VARCHAR(20) NOT NULL,
    context TEXT,
    decision TEXT NOT NULL,
    alternatives_considered TEXT,
    consequences TEXT,
    decision_date DATE,
    revisit_criteria TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT architecture_decision_status_valid
        CHECK (status IN ('PROPOSED', 'ACCEPTED', 'DEPRECATED', 'SUPERSEDED', 'REJECTED'))
);

CREATE INDEX idx_architecture_decision_project_id ON architecture_decision (project_id);
CREATE INDEX idx_architecture_decision_status ON architecture_decision (status);

-- Plain association tables: an unordered "this decision touches that
-- device/service" link with no extra attributes, so a join entity (like
-- task_dependency) would be unnecessary ceremony.
CREATE TABLE decision_related_device (
    decision_id UUID NOT NULL REFERENCES architecture_decision (id) ON DELETE CASCADE,
    device_id UUID NOT NULL REFERENCES device (id) ON DELETE CASCADE,
    PRIMARY KEY (decision_id, device_id)
);

CREATE TABLE decision_related_service (
    decision_id UUID NOT NULL REFERENCES architecture_decision (id) ON DELETE CASCADE,
    service_id UUID NOT NULL REFERENCES managed_service (id) ON DELETE CASCADE,
    PRIMARY KEY (decision_id, service_id)
);
