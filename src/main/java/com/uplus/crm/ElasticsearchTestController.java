package com.uplus.crm;

import lombok.RequiredArgsConstructor;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/es-test")
public class ElasticsearchTestController {

  private final RestClient restClient;

  @GetMapping("/analyze")
  public String analyze(@RequestParam String text) throws Exception {

    String body = """
        {
          "analyzer": "consult_index_analyzer",
          "text": "%s"
        }
        """.formatted(text);

    Request request = new Request("POST", "/consult-index/_analyze");
    request.setJsonEntity(body);

    Response response = restClient.performRequest(request);

    BufferedReader reader = new BufferedReader(
        new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8)
    );

    return reader.lines().collect(Collectors.joining());
  }
}