CREATE TABLE demo_requests (
    id UUID PRIMARY KEY,
    company_name TEXT NOT NULL,
    contact_name TEXT NOT NULL,
    email TEXT NOT NULL,
    country TEXT NOT NULL,
    phone TEXT,
    company_size TEXT NOT NULL CHECK (company_size IN ('1-10', '11-50', '51-200', '201-500', '500+')),
    message TEXT,
    status TEXT NOT NULL CHECK (status IN ('NEW', 'CONTACTED', 'CLOSED')),
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE demo_request_materials (
    demo_request_id UUID NOT NULL REFERENCES demo_requests(id) ON DELETE CASCADE,
    material TEXT NOT NULL CHECK (material IN ('STANDARD_FORMS', 'CHECKLIST', 'GAP_ANALYSIS')),
    PRIMARY KEY (demo_request_id, material)
);

CREATE INDEX idx_demo_requests_email
    ON demo_requests(email);
CREATE INDEX idx_demo_requests_status_created_at
    ON demo_requests(status, created_at);
