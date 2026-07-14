-- Milestone 7: a single local administrator account. No default password
-- is ever inserted here — see ADR-0008. AdminAccountInitializer creates
-- the one row from ADMIN_USERNAME/ADMIN_PASSWORD environment variables on
-- startup, only when the table is empty.

CREATE TABLE admin_user (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(200) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
