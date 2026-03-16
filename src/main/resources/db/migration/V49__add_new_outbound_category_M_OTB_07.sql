-- Description: M_OTB_07 신규가입권유 카테고리 추가 (정렬 순서 포함)

INSERT INTO consultation_category_policy (
    category_code,
    large_category,
    medium_category,
    small_category,
    is_active,
    sort_order
) VALUES (
             'M_OTB_07',
             '아웃바운드',
             '신규가입권유',
             '신규가입',
             1,
             707
         );