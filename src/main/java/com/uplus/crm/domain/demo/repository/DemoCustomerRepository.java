package com.uplus.crm.domain.demo.repository;

import com.uplus.crm.domain.consultation.entity.Customer;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DemoCustomerRepository extends JpaRepository<Customer, Long> {

    interface SubscribedProductProjection {
        String getProductType();
        String getProductCode();
        String getProductName();
        String getCategory();
    }

    /**
     * 고객의 현재 활성 가입 상품(인터넷/TV·모바일·부가서비스)을 한 번에 조회한다.
     * V8 이후 스키마 기준: customer_contracts에 customer_id가 없으므로
     * 각 subscription 테이블의 customer_id로 직접 조회한다.
     * extinguish_at / extinguish_date 가 NULL인 행만 대상으로 한다.
     */
    @Query(value = """
            SELECT 'HOME'             AS product_type,
                   ph.home_code        AS product_code,
                   ph.product_name,
                   ph.category
            FROM   customer_subscription_home sh
            JOIN   product_home ph ON sh.home_code = ph.home_code
            WHERE  sh.customer_id = :customerId AND sh.extinguish_at IS NULL
            UNION ALL
            SELECT 'MOBILE'           AS product_type,
                   pm.mobile_code      AS product_code,
                   pm.plan_name        AS product_name,
                   pm.category
            FROM   customer_subscription_mobile sm
            JOIN   product_mobile pm ON sm.mobile_code = pm.mobile_code
            WHERE  sm.customer_id = :customerId AND sm.extinguish_at IS NULL
            UNION ALL
            SELECT 'ADDITIONAL'       AS product_type,
                   pa.additional_code  AS product_code,
                   pa.additional_name  AS product_name,
                   pa.category
            FROM   customer_subscription_additional sa
            JOIN   product_additional pa ON sa.service_code = pa.additional_code
            WHERE  sa.customer_id = :customerId AND sa.extinguish_date IS NULL
            """, nativeQuery = true)
    List<SubscribedProductProjection> findActiveSubscribedProducts(@Param("customerId") Long customerId);
}
