-- V2__soft_delete_retention.sql
ALTER TABLE chat_sessions
  ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMPTZ NULL;

CREATE INDEX IF NOT EXISTS idx_chat_sessions_user_deleted ON chat_sessions(user_id, deleted_at);
