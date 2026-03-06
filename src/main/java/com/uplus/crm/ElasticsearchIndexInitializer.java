package com.uplus.crm;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RestClient;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Component
@RequiredArgsConstructor
public class ElasticsearchIndexInitializer {

  private final RestClient restClient;

  @PostConstruct
  public void createIndex() throws Exception {

    InputStream is = getClass()
        .getClassLoader()
        .getResourceAsStream("elasticsearch/consult-settings.json");

    if (is == null) {
      throw new IllegalStateException("consult-settings.json not found in resources");
    }

    String json = new String(is.readAllBytes());

    Request request = new Request("PUT", "/consult-index");
    request.setJsonEntity(json);

    try {
      restClient.performRequest(request);
      System.out.println("Elasticsearch index created: consult-index");
    } catch (Exception e) {
      System.out.println("Elasticsearch index creat FAILED!!!");
      e.printStackTrace();
    }
  }
}