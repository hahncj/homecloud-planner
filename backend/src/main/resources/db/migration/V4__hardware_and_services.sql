-- Milestone 4: hardware inventory and the managed-service catalog.

CREATE TABLE device (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL REFERENCES project (id) ON DELETE RESTRICT,
    name VARCHAR(200) NOT NULL,
    manufacturer VARCHAR(200),
    model VARCHAR(200),
    serial_number VARCHAR(200),
    role VARCHAR(100),
    location VARCHAR(200),
    hostname VARCHAR(255),
    ip_address VARCHAR(45),
    mac_address VARCHAR(17),
    vlan INTEGER,
    operating_system VARCHAR(200),
    firmware_version VARCHAR(100),
    purchase_date DATE,
    warranty_expiration DATE,
    lifecycle_status VARCHAR(20) NOT NULL,
    replacement_target DATE,
    notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT device_lifecycle_status_valid
        CHECK (lifecycle_status IN ('PLANNED', 'ACTIVE', 'SPARE', 'MAINTENANCE', 'RETIRED', 'DISPOSED')),
    CONSTRAINT device_vlan_range CHECK (vlan IS NULL OR (vlan >= 1 AND vlan <= 4094))
);

CREATE INDEX idx_device_project_id ON device (project_id);
CREATE INDEX idx_device_lifecycle_status ON device (lifecycle_status);

-- Domain name is ManagedService (not Service) to avoid confusion with
-- infrastructure/platform "services"; the table follows the same naming.
CREATE TABLE managed_service (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL REFERENCES project (id) ON DELETE RESTRICT,
    host_device_id UUID REFERENCES device (id) ON DELETE SET NULL,
    name VARCHAR(200) NOT NULL,
    purpose VARCHAR(500),
    description TEXT,
    status VARCHAR(20) NOT NULL,
    runtime_type VARCHAR(20) NOT NULL,
    storage_location VARCHAR(200),
    sensitivity VARCHAR(20) NOT NULL,
    externally_exposed BOOLEAN NOT NULL DEFAULT FALSE,
    authentication_method VARCHAR(200),
    backup_policy VARCHAR(500),
    documentation_url VARCHAR(2048),
    repository_url VARCHAR(2048),
    notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT managed_service_status_valid CHECK (status IN
        ('PLANNED', 'INSTALLING', 'CONFIGURING', 'VALIDATING', 'OPERATIONAL', 'DEGRADED', 'DISABLED', 'RETIRED')),
    CONSTRAINT managed_service_runtime_type_valid CHECK (runtime_type IN
        ('DOCKER', 'KUBERNETES', 'VIRTUAL_MACHINE', 'BARE_METAL', 'MANAGED_CLOUD', 'NAS_NATIVE', 'OTHER')),
    CONSTRAINT managed_service_sensitivity_valid CHECK (sensitivity IN
        ('PUBLIC', 'INTERNAL', 'CONFIDENTIAL', 'HIGHLY_SENSITIVE'))
);

CREATE INDEX idx_managed_service_project_id ON managed_service (project_id);
CREATE INDEX idx_managed_service_host_device_id ON managed_service (host_device_id);
CREATE INDEX idx_managed_service_status ON managed_service (status);

-- Dependency rows are relationship records tied to the lifecycle of both
-- services (see task_dependency in V2 for the same reasoning). Whether a
-- service *itself* can be deleted while other services depend on it is an
-- application-layer rule (see ADR-0004) so the resulting problem detail is
-- clear, rather than surfacing a raw FK violation.
CREATE TABLE service_dependency (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    service_id UUID NOT NULL REFERENCES managed_service (id) ON DELETE CASCADE,
    depends_on_service_id UUID NOT NULL REFERENCES managed_service (id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT service_dependency_no_self CHECK (service_id <> depends_on_service_id),
    CONSTRAINT service_dependency_unique UNIQUE (service_id, depends_on_service_id)
);

CREATE INDEX idx_service_dependency_service_id ON service_dependency (service_id);
CREATE INDEX idx_service_dependency_depends_on_service_id ON service_dependency (depends_on_service_id);
