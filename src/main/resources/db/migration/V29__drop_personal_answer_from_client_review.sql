-- V29__drop_personal_answer_from_client_review.sql

SET @col_exists := (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'client_review'
    AND COLUMN_NAME = 'personal_answer'
);

SET @sql := IF(
  @col_exists > 0,
  'ALTER TABLE client_review DROP COLUMN personal_answer',
  'SELECT 1'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;