package com.uplus.crm;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "① ES 셋업")
@RestController
@RequiredArgsConstructor
@RequestMapping("/es-test")
public class ElasticsearchTestController {

  private final RestClient restClient;
  private final ElasticsearchIndexInitializer indexInitializer;

  @Operation(
      summary = "[셋업 Step 1] 인덱스 재생성 — 사전 변경 시에만",
      description = """
          기존 consult-index를 삭제하고 최신 분석기 설정으로 재생성합니다.
          사전 파일(analysis_synonyms.txt, userdict.txt 등)을 수정한 후에만 호출하세요.
          평소 앱 재시작 시에는 불필요합니다. ES 데이터는 es_data 볼륨에 유지됩니다.

          [로직]
          기존 consult-index 삭제 → consult-settings.json(분석기 정의) + consult-mapping.json(필드) 으로 재생성

          [참조 파일]
          읽기: src/main/resources/elasticsearch/consult-settings.json
          읽기: src/main/resources/elasticsearch/consult-mapping.json
          쓰기: Elasticsearch consult-index (기존 데이터 전체 삭제 후 재생성)

          [실행 후 필수]
          POST /admin/es/sync → 데이터 재적재

          [이 증상 발생 시 호출]
          - 'failed to find analyzer [korean_index_analyzer]' 오류
          - 검색 결과가 모두 빈 배열
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
      tags = {"③ ES 분석"},
      summary = "[사전 검증] 토크나이징 결과 확인",
      description = """
          입력 텍스트가 분석기에서 어떤 토큰으로 변환되는지 확인합니다.
          사전 파일 수정 + 인덱스 재생성 후 정상 반영 여부를 검증할 때 사용합니다.

          [참조 사전]
          - korean_index_analyzer         : synonyms.txt (검색 동의어), userdict.txt
          - korean_analysis_index_analyzer: analysis_synonyms.txt (분석 동의어), analysis_userdict.txt

          [검색용 사전(synonyms.txt) 확인 예시]
          - 갤폰   → [갤럭시]
          - 번이   → [번호이동]
          - 꼼수   → [정책악용]
          - 넷플   → [넷플릭스]

          [분석용 사전(analysis_synonyms.txt) 확인 예시]
          - 안녕하세요         → [인사말]
          - 감사합니다         → [감사인사]
          - 잠시기다려주세요   → [대기안내]
          - 충분히이해합니다   → [공감응대]
          - 친절하게해주셨다   → [친절응대]

          [참조 DB]
          Elasticsearch consult-index (_analyze API 직접 호출, DB 조회 없음)
          """
  )
  @GetMapping("/analyze")
  public ResponseEntity<String> analyze(
      @Parameter(description = "분석할 텍스트", example = "갤폰 번이 해지")
      @RequestParam String text,
      @Parameter(description = "분석기 이름 (korean_index_analyzer / korean_analysis_index_analyzer)", example = "korean_index_analyzer")
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
