-- V3__users_roles.sql
-- Add DB-backed user identities + role assignments for API-key mode authorization.

CREATE TABLE IF NOT EXISTS app_users (
  id UUID PRIMARY KEY,
  user_id VARCHAR(128) NOT NULL UNIQUE,
  display_name VARCHAR(200) NULL,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_app_users_user_id ON app_users(user_id);

CREATE TABLE IF NOT EXISTS app_user_roles (
  app_user_id UUID NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
  role VARCHAR(64) NOT NULL,
  PRIMARY KEY (app_user_id, role)
);

CREATE INDEX IF NOT EXISTS idx_app_user_roles_role ON app_user_roles(role);

-- Seed admin for local development/testing.
-- admin (read/write/delete)
INSERT INTO app_users (id, user_id, display_name, active)
VALUES
  ('00000000-0000-0000-0000-000000000001', 'admin', 'User One', TRUE)
ON CONFLICT (user_id) DO NOTHING;

-- admin roles
INSERT INTO app_user_roles (app_user_id, role)
VALUES
  ('00000000-0000-0000-0000-000000000001', 'CHAT_ADMIN'),
  ('00000000-0000-0000-0000-000000000001', 'CHAT_READ'),
  ('00000000-0000-0000-0000-000000000001', 'CHAT_WRITE'),
  ('00000000-0000-0000-0000-000000000001', 'CHAT_DELETE')
ON CONFLICT DO NOTHING;

