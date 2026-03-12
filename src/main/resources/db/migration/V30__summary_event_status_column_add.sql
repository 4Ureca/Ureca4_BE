ALTER TABLE summary_event_status
ADD COLUMN fail_reason TEXT NULL COMMENT '실패 원인'
AFTER status;