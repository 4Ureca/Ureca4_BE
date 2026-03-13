package com.uplus.crm.domain.summary.document.es;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Getter
@Setter
@Document(indexName = "consult-search-index")
public class ConsultSearchDocument {

  @Id
  private String id;

  @Field(type = FieldType.Keyword)
  private String consultId;
}
