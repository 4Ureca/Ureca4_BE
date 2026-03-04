-- V17__modify_customers_preferred_contact.sql

-- 1. 기존 더미데이터 내용 변경
UPDATE customers
SET preferred_contact = CASE
    WHEN LOWER(preferred_contact) IN ('email','call') THEN 'CALL'
    WHEN LOWER(preferred_contact) = 'push' THEN 'CHATTING'
    ELSE NULL
END;

-- 2. ENUM 타입 변경
ALTER TABLE customers
MODIFY COLUMN preferred_contact
ENUM('CALL','CHATTING')
NULL
COMMENT '선호 연락 수단';