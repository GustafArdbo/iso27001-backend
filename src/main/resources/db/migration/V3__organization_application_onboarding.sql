DROP TABLE IF EXISTS demo_request_materials;
DROP TABLE IF EXISTS demo_requests;

CREATE TABLE organization_applications (
    id UUID PRIMARY KEY,
    company_name TEXT NOT NULL,
    owner_name TEXT NOT NULL,
    owner_email TEXT NOT NULL,
    country TEXT NOT NULL,
    phone TEXT,
    company_size TEXT NOT NULL CHECK (company_size IN ('1-10', '11-50', '51-200', '201-500', '500+')),
    message TEXT,
    application_status TEXT NOT NULL CHECK (application_status IN ('SUBMITTED', 'APPROVED', 'REJECTED')),
    invitation_status TEXT NOT NULL CHECK (invitation_status IN ('NOT_SENT', 'SENT', 'FAILED', 'ACCEPTED')),
    organization_id UUID REFERENCES organizations(id),
    owner_profile_id UUID REFERENCES user_profiles(id),
    approved_by_supabase_user_id UUID,
    approved_at TIMESTAMP,
    rejected_by_supabase_user_id UUID,
    rejected_at TIMESTAMP,
    rejection_reason TEXT,
    invitation_sent_at TIMESTAMP,
    invitation_accepted_at TIMESTAMP,
    invitation_failure_reason TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE organization_application_materials (
    organization_application_id UUID NOT NULL REFERENCES organization_applications(id) ON DELETE CASCADE,
    material TEXT NOT NULL CHECK (material IN ('STANDARD_FORMS', 'CHECKLIST', 'GAP_ANALYSIS')),
    PRIMARY KEY (organization_application_id, material)
);

CREATE INDEX idx_organization_applications_owner_email
    ON organization_applications(owner_email);
CREATE INDEX idx_organization_applications_status_created_at
    ON organization_applications(application_status, created_at);
CREATE INDEX idx_organization_applications_owner_profile_id
    ON organization_applications(owner_profile_id);
