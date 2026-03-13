package com.uplus.crm.domain.summary.repository.es;

import com.uplus.crm.domain.summary.document.es.ConsultSearchDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ConsultSearchRepository
    extends ElasticsearchRepository<ConsultSearchDocument, String> {
}
