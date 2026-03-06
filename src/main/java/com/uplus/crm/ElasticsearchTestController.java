package com.uplus.crm;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseException;
import org.elasticsearch.client.RestClient;
import org.springframework.http.ResponseEntity;
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
  private final ElasticsearchIndexInitializer indexInitializer;

  @Operation(
      summary = "[Step 0] 인덱스 강제 재생성",
      description = """
          기존 consult-index를 삭제하고 최신 settings(분석기)/mappings(필드 타입)로 재생성합니다.
          아래 증상이 하나라도 있으면 이 API를 먼저 호출하세요:

          - GET /es-test/analyze 에서 'failed to find analyzer [korean_index_analyzer]' 오류
          - sentiment / priority 검색 결과가 빈 배열
          - date 검색에서 500 오류
          - customerId 검색 결과가 빈 배열

          호출 순서:
          1. POST /es-test/recreate-index  ← 인덱스 재생성
          2. POST /elasticsearch/consult/test-data  ← 테스트 데이터 재적재
          3. 각 검색 API 테스트
          """
  )
  @PostMapping("/recreate-index")
  public ResponseEntity<String> recreateIndex() {
    try {
      String result = indexInitializer.forceRecreate();
      return ResponseEntity.ok(result);
    } catch (Exception e) {
      return ResponseEntity.internalServerError()
          .body("인덱스 재생성 실패: " + e.getMessage());
    }
  }

  @Operation(
      summary = "분석기 토크나이징 결과 확인",
      description = """
          입력 텍스트를 지정한 분석기로 처리한 결과(토큰 목록)를 반환합니다.
          동의어 사전 및 불용어 사전이 정상 적용되는지 확인할 때 사용합니다.

          analyzer 종류:
          - korean_index_analyzer (기본값): 인덱싱 시 적용되는 분석기. synonym 필터 사용.
          - korean_search_analyzer: 검색 시 적용되는 분석기. synonym_graph 필터 사용.

          동의어 확인 테스트 예시:
          - text=갤폰  →  토큰: [갤럭시]          (갤폰 → 갤럭시 동의어 확장)
          - text=번이  →  토큰: [번호이동]
          - text=넷플  →  토큰: [넷플릭스]
          - text=디플  →  토큰: [디즈니플러스]
          - text=기변  →  토큰: [기기변경]
          - text=아폰  →  토큰: [아이폰]
          - text=갤탭  →  토큰: [갤럭시탭]
          - text=셋탑  →  토큰: [셋톱박스]
          - text=리모콘  →  토큰: [리모컨]

          불용어 확인 테스트 예시:
          - text=감사합니다  →  합니다 토큰 제거됨
          - text=어떻게 도와드릴까요  →  어떻게, 드리 토큰 제거됨
          """
  )
  @GetMapping("/analyze")
  public ResponseEntity<String> analyze(
      @Parameter(description = "분석할 텍스트 (구어체·축약어 입력 가능)", example = "갤폰 번이 해지")
      @RequestParam String text,
      @Parameter(description = "사용할 분석기 이름 (korean_index_analyzer / korean_search_analyzer)", example = "korean_index_analyzer")
      @RequestParam(defaultValue = "korean_index_analyzer") String analyzer) {

    try {
      String body = """
          {
            "analyzer": "%s",
            "text": "%s"
          }
          """.formatted(analyzer, text);

      Request request = new Request("POST", "/consult-index/_analyze");
      request.setJsonEntity(body);

      Response response = restClient.performRequest(request);

      String result = new BufferedReader(
          new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8)
      ).lines().collect(Collectors.joining());

      return ResponseEntity.ok(result);

    } catch (ResponseException e) {
      return ResponseEntity.status(e.getResponse().getStatusLine().getStatusCode())
          .body("ES 오류: " + e.getMessage());
    } catch (Exception e) {
      return ResponseEntity.internalServerError()
          .body("분석 실패: " + e.getMessage());
    }
  }
}