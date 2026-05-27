ALTER TABLE app_users
    ADD COLUMN supabase_user_id UUID;

CREATE INDEX idx_app_users_supabase_user_id
    ON app_users(supabase_user_id);

CREATE UNIQUE INDEX uk_app_users_organization_supabase_user_id
    ON app_users(organization_id, supabase_user_id);

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
