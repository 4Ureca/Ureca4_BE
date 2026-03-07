-- ============================================================
-- V23: menus 테이블 초기 데이터 적재
-- ============================================================

INSERT INTO menus (menu_code, menu_desc)
SELECT t.menu_code, t.menu_desc
FROM (
    SELECT 'MENU_CONSULT_HISTORY'      AS menu_code, '분류: 상담업무 / 메뉴명: 상담내역' AS menu_desc
    UNION ALL
    SELECT 'MENU_CONSULT_RESULT_WRITE' AS menu_code, '분류: 상담업무 / 메뉴명: 결과서 작성' AS menu_desc
    UNION ALL
    SELECT 'MENU_CONSULT_SUMMARY'      AS menu_code, '분류: 상담업무 / 메뉴명: 상담요약' AS menu_desc
    UNION ALL
    SELECT 'MENU_CONSULT_ANALYSIS'     AS menu_code, '분류: 상담업무 / 메뉴명: 상담 분석' AS menu_desc
    UNION ALL
    SELECT 'MENU_DASHBOARD_POLICY'     AS menu_code, '분류: 대시보드 / 메뉴명: 운영정책' AS menu_desc
    UNION ALL
    SELECT 'MENU_DASHBOARD_MANUAL'     AS menu_code, '분류: 대시보드 / 메뉴명: 전체 메뉴얼' AS menu_desc
    UNION ALL
    SELECT 'MENU_DASHBOARD_CUSTOMER_TYPE' AS menu_code, '분류: 대시보드 / 메뉴명: 고객유형별 대응' AS menu_desc
    UNION ALL
    SELECT 'MENU_DASHBOARD_BEST_PRACTICE' AS menu_code, '분류: 대시보드 / 메뉴명: 우수 상담 사례' AS menu_desc
    UNION ALL
    SELECT 'MENU_DASHBOARD_NOTICE'     AS menu_code, '분류: 대시보드 / 메뉴명: 공지사항' AS menu_desc
    UNION ALL
	SELECT 'MENU_DASHBOARD_NOTIFICATION_SEND' AS menu_code, '분류: 대시보드 / 메뉴명: 알림 발송' AS menu_desc
	UNION ALL
    SELECT 'MENU_ADMIN_EMPLOYEE_ACCOUNT' AS menu_code, '분류: 관리 / 메뉴명: 직원 계정 관리' AS menu_desc
    UNION ALL
    SELECT 'MENU_ADMIN_MY_ACCOUNT'     AS menu_code, '분류: 관리 / 메뉴명: 내 계정 관리' AS menu_desc
) t
LEFT JOIN menus m
       ON m.menu_code = t.menu_code
      AND m.is_deleted = 0
WHERE m.menu_id IS NULL;