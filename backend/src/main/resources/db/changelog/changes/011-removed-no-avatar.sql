ALTER TABLE users ALTER COLUMN avatar_url DROP NOT NULL;
ALTER TABLE users ALTER COLUMN avatar_url DROP DEFAULT;
UPDATE users SET avatar_url = NULL WHERE avatar_url = 'no-avatar';