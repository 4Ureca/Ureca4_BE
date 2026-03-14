-- analysis_code: 인바운드 불만 카테고리(complaint_category) 초기 데이터 삽입

INSERT INTO analysis_code (code_name, classification)
VALUES
    ('OTHER',         'complaint_category'),
    ('SVC_FAULT',     'complaint_category'),
    ('FEE_INQUIRY',   'complaint_category'),
    ('DEVICE_CHANGE', 'complaint_category'),
    ('ADDON_CHG',     'complaint_category')
    ON DUPLICATE KEY UPDATE
                         code_name = VALUES(code_name),
                         classification = VALUES(classification);