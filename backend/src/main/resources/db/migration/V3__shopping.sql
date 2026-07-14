-- Milestone 3: shopping list / purchase items behind project budgets.

CREATE TABLE purchase_item (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL REFERENCES project (id) ON DELETE RESTRICT,
    phase_id UUID REFERENCES phase (id) ON DELETE SET NULL,
    category VARCHAR(100) NOT NULL,
    product_name VARCHAR(200) NOT NULL,
    manufacturer VARCHAR(200),
    model VARCHAR(200),
    description TEXT,
    quantity INTEGER NOT NULL,
    estimated_unit_price NUMERIC(12, 2),
    actual_unit_price NUMERIC(12, 2),
    vendor VARCHAR(200),
    purchase_url VARCHAR(2048),
    status VARCHAR(20) NOT NULL,
    purchase_date DATE,
    delivery_date DATE,
    warranty_expiration DATE,
    receipt_reference VARCHAR(500),
    notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT purchase_item_status_valid
        CHECK (status IN ('IDEA', 'RESEARCHING', 'PLANNED', 'ORDERED', 'RECEIVED', 'INSTALLED', 'CANCELLED')),
    CONSTRAINT purchase_item_quantity_positive CHECK (quantity >= 1),
    CONSTRAINT purchase_item_estimated_price_non_negative CHECK (estimated_unit_price IS NULL OR estimated_unit_price >= 0),
    CONSTRAINT purchase_item_actual_price_non_negative CHECK (actual_unit_price IS NULL OR actual_unit_price >= 0)
);

CREATE INDEX idx_purchase_item_project_id ON purchase_item (project_id);
CREATE INDEX idx_purchase_item_phase_id ON purchase_item (phase_id);
CREATE INDEX idx_purchase_item_status ON purchase_item (status);
CREATE INDEX idx_purchase_item_category ON purchase_item (category);
