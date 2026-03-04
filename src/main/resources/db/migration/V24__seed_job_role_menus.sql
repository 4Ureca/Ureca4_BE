-- ============================================================
-- V24: job_role_menus 테이블 초기 데이터 적재
-- 전제:
--   1) job_roles 테이블에 role_name 컬럼이 존재
--   2) role_name 값이 '관리자', '상담사'
-- ============================================================

INSERT INTO job_role_menus (job_role_id, menu_id)
SELECT jr.job_role_id, m.menu_id
FROM (
    -- 관리자 권한
    SELECT '관리자' AS role_name, 'MENU_CONSULT_HISTORY' AS menu_code
    UNION ALL
    SELECT '관리자', 'MENU_CONSULT_SUMMARY'
    UNION ALL
    SELECT '관리자', 'MENU_CONSULT_ANALYSIS'
    UNION ALL
    SELECT '관리자', 'MENU_DASHBOARD_POLICY'
    UNION ALL
    SELECT '관리자', 'MENU_DASHBOARD_MANUAL'
    UNION ALL
    SELECT '관리자', 'MENU_DASHBOARD_CUSTOMER_TYPE'
    UNION ALL
    SELECT '관리자', 'MENU_DASHBOARD_BEST_PRACTICE'
    UNION ALL
    SELECT '관리자', 'MENU_DASHBOARD_NOTICE'
    UNION ALL
    SELECT '관리자', 'MENU_ADMIN_EMPLOYEE_ACCOUNT'
    UNION ALL
    SELECT '관리자', 'MENU_ADMIN_MY_ACCOUNT'

    UNION ALL

    -- 상담사 권한
    SELECT '상담사', 'MENU_CONSULT_HISTORY'
    UNION ALL
    SELECT '상담사', 'MENU_CONSULT_RESULT_WRITE'
    UNION ALL
    SELECT '상담사', 'MENU_CONSULT_SUMMARY'
    UNION ALL
    SELECT '상담사', 'MENU_CONSULT_ANALYSIS'
    UNION ALL
    SELECT '상담사', 'MENU_DASHBOARD_POLICY'
    UNION ALL
    SELECT '상담사', 'MENU_DASHBOARD_MANUAL'
    UNION ALL
    SELECT '상담사', 'MENU_DASHBOARD_CUSTOMER_TYPE'
    UNION ALL
    SELECT '상담사', 'MENU_DASHBOARD_BEST_PRACTICE'
    UNION ALL
    SELECT '상담사', 'MENU_DASHBOARD_NOTICE'
    UNION ALL
    SELECT '상담사', 'MENU_ADMIN_MY_ACCOUNT'
) t
JOIN job_roles jr
  ON jr.role_name = t.role_name
JOIN menus m
  ON m.menu_code = t.menu_code
 AND m.is_deleted = 0
LEFT JOIN job_role_menus jrm
  ON jrm.job_role_id = jr.job_role_id
 AND jrm.menu_id = m.menu_id
WHERE jrm.role_menu_id IS NULL;