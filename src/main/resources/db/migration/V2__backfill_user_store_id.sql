-- This migration backfills all existing admin users so their store_id
-- correctly points to the store they administer.

UPDATE users u
JOIN stores s ON s.store_admin_id = u.id
SET u.store_id = s.id
WHERE u.store_id IS NULL;