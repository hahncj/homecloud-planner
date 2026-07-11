-- Baseline migration: proves Flyway is wired up before any domain schema exists.
CREATE TABLE schema_baseline (
    id SMALLINT PRIMARY KEY DEFAULT 1,
    applied_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT schema_baseline_singleton CHECK (id = 1)
);

INSERT INTO schema_baseline (id) VALUES (1);
