package com.uplus.crm.domain.elasticsearch.repository;

import com.uplus.crm.domain.elasticsearch.entity.ConsultDoc;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConsultElasticRepository extends ElasticsearchRepository<ConsultDoc, String> {
    // 이제 save(), findById() 등을 별도 구현 없이 사용할 수 있습니다.
    List<ConsultDoc> findByCustomerId(String customerId);
}