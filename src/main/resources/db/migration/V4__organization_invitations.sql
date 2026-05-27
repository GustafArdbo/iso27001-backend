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
    invited_by_user_id UUID NOT NULL REFERENCES app_users(id),
    accepted_by_user_id UUID REFERENCES app_users(id),
    revoked_by_user_id UUID REFERENCES app_users(id),
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX uk_organization_invitations_token_hash
    ON organization_invitations(token_hash);

CREATE INDEX idx_organization_invitations_organization_id
    ON organization_invitations(organization_id);

CREATE INDEX idx_organization_invitations_organization_email_status
    ON organization_invitations(organization_id, email, status);
