CREATE TABLE organizations (
    id UUID PRIMARY KEY,
    name TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE app_users (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations(id),
    email TEXT NOT NULL,
    role TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE assessments (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations(id),
    name TEXT NOT NULL,
    status TEXT NOT NULL CHECK (status IN ('DRAFT', 'IN_PROGRESS', 'COMPLETED')),
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE assessment_answers (
    id UUID PRIMARY KEY,
    assessment_id UUID NOT NULL REFERENCES assessments(id),
    control_id TEXT NOT NULL,
    answer TEXT NOT NULL CHECK (answer IN ('YES', 'PARTIAL', 'NO', 'NOT_APPLICABLE')),
    comment TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_app_users_organization_id ON app_users(organization_id);
CREATE INDEX idx_assessments_organization_id ON assessments(organization_id);
CREATE INDEX idx_assessment_answers_assessment_id ON assessment_answers(assessment_id);
CREATE UNIQUE INDEX uk_assessment_answers_assessment_control
    ON assessment_answers(assessment_id, control_id);
