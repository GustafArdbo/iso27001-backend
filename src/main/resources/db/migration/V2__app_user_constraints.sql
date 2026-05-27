ALTER TABLE app_users
    ADD CONSTRAINT chk_app_users_role
    CHECK (role IN ('OWNER', 'ADMIN', 'AUDITOR', 'MEMBER', 'VIEWER'));

CREATE UNIQUE INDEX uk_app_users_organization_email
    ON app_users(organization_id, email);
