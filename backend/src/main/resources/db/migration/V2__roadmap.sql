-- Milestone 2: projects, ordered phases, tasks, and task dependencies.

CREATE TABLE project (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(200) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL,
    budget NUMERIC(12, 2),
    start_date DATE,
    target_date DATE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT project_status_valid CHECK (status IN ('PLANNING', 'IN_PROGRESS', 'ON_HOLD', 'COMPLETED', 'CANCELLED')),
    CONSTRAINT project_budget_non_negative CHECK (budget IS NULL OR budget >= 0)
);

CREATE TABLE phase (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL REFERENCES project (id) ON DELETE RESTRICT,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    sequence INTEGER NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT phase_sequence_positive CHECK (sequence >= 1),
    CONSTRAINT phase_project_sequence_unique UNIQUE (project_id, sequence)
);

CREATE INDEX idx_phase_project_id ON phase (project_id);

CREATE TABLE task (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    phase_id UUID NOT NULL REFERENCES phase (id) ON DELETE RESTRICT,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL,
    priority VARCHAR(20) NOT NULL,
    estimated_cost NUMERIC(12, 2),
    actual_cost NUMERIC(12, 2),
    target_date DATE,
    completed_date DATE,
    acceptance_criteria TEXT,
    notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT task_status_valid CHECK (status IN ('NOT_STARTED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED')),
    CONSTRAINT task_priority_valid CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    CONSTRAINT task_estimated_cost_non_negative CHECK (estimated_cost IS NULL OR estimated_cost >= 0),
    CONSTRAINT task_actual_cost_non_negative CHECK (actual_cost IS NULL OR actual_cost >= 0)
);

CREATE INDEX idx_task_phase_id ON task (phase_id);
CREATE INDEX idx_task_status ON task (status);

-- Dependency rows are relationship records tied to the lifecycle of both
-- tasks; they carry no meaning once either task is gone, so both sides
-- cascade. Business rules (self-dependency, duplicates, cross-project,
-- cycles) are enforced in the application layer where the full project
-- graph is available.
CREATE TABLE task_dependency (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    task_id UUID NOT NULL REFERENCES task (id) ON DELETE CASCADE,
    depends_on_task_id UUID NOT NULL REFERENCES task (id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT task_dependency_no_self CHECK (task_id <> depends_on_task_id),
    CONSTRAINT task_dependency_unique UNIQUE (task_id, depends_on_task_id)
);

CREATE INDEX idx_task_dependency_task_id ON task_dependency (task_id);
CREATE INDEX idx_task_dependency_depends_on_task_id ON task_dependency (depends_on_task_id);
