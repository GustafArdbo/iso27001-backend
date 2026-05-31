CREATE TABLE organizations (
    id UUID PRIMARY KEY,
    name TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE user_profiles (
    id UUID PRIMARY KEY,
    supabase_user_id UUID,
    email TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE organization_memberships (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations(id),
    user_profile_id UUID NOT NULL REFERENCES user_profiles(id),
    role TEXT NOT NULL CHECK (role IN ('OWNER', 'ADMIN', 'AUDITOR', 'MEMBER', 'VIEWER')),
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

CREATE TABLE auth_revocations (
    id UUID PRIMARY KEY,
    revocation_type TEXT NOT NULL CHECK (revocation_type IN ('TOKEN', 'SESSION')),
    token_hash TEXT,
    jwt_id TEXT,
    session_id UUID,
    subject TEXT,
    reason TEXT,
    expires_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE organization_invitations (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations(id),
    email TEXT NOT NULL,
    role TEXT NOT NULL CHECK (role IN ('OWNER', 'ADMIN', 'AUDITOR', 'MEMBER', 'VIEWER')),
    token_hash TEXT NOT NULL,
    status TEXT NOT NULL CHECK (status IN ('PENDING', 'ACCEPTED', 'REVOKED', 'EXPIRED')),
    expires_at TIMESTAMP NOT NULL,
    accepted_at TIMESTAMP,
    revoked_at TIMESTAMP,
    invited_by_membership_id UUID NOT NULL REFERENCES organization_memberships(id),
    accepted_by_membership_id UUID REFERENCES organization_memberships(id),
    revoked_by_membership_id UUID REFERENCES organization_memberships(id),
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX uk_user_profiles_supabase_user_id
    ON user_profiles(supabase_user_id);
CREATE INDEX idx_user_profiles_email
    ON user_profiles(email);
CREATE UNIQUE INDEX uk_organization_memberships_organization_profile
    ON organization_memberships(organization_id, user_profile_id);
CREATE INDEX idx_organization_memberships_organization_id
    ON organization_memberships(organization_id);
CREATE INDEX idx_organization_memberships_user_profile_id
    ON organization_memberships(user_profile_id);
CREATE INDEX idx_assessments_organization_id ON assessments(organization_id);
CREATE INDEX idx_assessment_answers_assessment_id ON assessment_answers(assessment_id);
CREATE UNIQUE INDEX uk_assessment_answers_assessment_control
    ON assessment_answers(assessment_id, control_id);
CREATE INDEX idx_auth_revocations_token_hash
    ON auth_revocations(token_hash);
CREATE INDEX idx_auth_revocations_jwt_id
    ON auth_revocations(jwt_id);
CREATE INDEX idx_auth_revocations_session_id
    ON auth_revocations(session_id);
CREATE INDEX idx_auth_revocations_subject
    ON auth_revocations(subject);
CREATE INDEX idx_auth_revocations_expires_at
    ON auth_revocations(expires_at);
CREATE UNIQUE INDEX uk_organization_invitations_token_hash
    ON organization_invitations(token_hash);
CREATE INDEX idx_organization_invitations_organization_id
    ON organization_invitations(organization_id);
CREATE INDEX idx_organization_invitations_organization_email_status
    ON organization_invitations(organization_id, email, status);
