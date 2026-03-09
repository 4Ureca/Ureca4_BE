package com.uplus.crm.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

  @Bean
  public OpenAPI openAPI() {
    String jwtSchemeName = "bearerAuth";
    SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwtSchemeName);

    Components components = new Components()
            .addSecuritySchemes(jwtSchemeName, new SecurityScheme()
                    .name(jwtSchemeName)
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT"));

    return new OpenAPI()
            .info(new Info()
                    .title("고객 상담 기록 관리 서비스 API")
                    .description("고객 상담 기록 관리 서비스의 API입니다. 구글 로그인을 통해 토큰을 발급받으세요.")
                    .version("1.0.0"))
            .addSecurityItem(securityRequirement)
            .components(components)
            // ── Elasticsearch 관련 태그 노출 순서 고정 ──────────────────────────
            .tags(List.of(
                    new Tag().name("① ES 셋업")
                            .description("""
                                    [언제] 최초 1회 / 사전 파일 변경 후 / 데이터 재동기화 필요 시
                                    [권한] /admin/** 는 관리자(ROLE_ADMIN)만 접근 가능

                                    [상황별 실행 순서]

                                    A. 최초 환경 세팅 (처음 실행):
                                       1. docker compose up -d
                                       2. ./gradlew bootRun
                                       3. POST /admin/es/sync  → Batch로 생성된 로컬 DB 데이터를 ES에 적재

                                    B. 사전 파일(userdict.txt, analysis_synonyms.txt 등) 수정 시:
                                       1. docker compose restart elasticsearch
                                       2. ./gradlew bootRun
                                       3. POST /es-test/recreate-index  → 인덱스 재생성 (분석기 재적용)
                                       4. POST /admin/es/sync           → 데이터 재적재

                                    C. 평소 앱 재시작 (변경 없음):
                                       별도 작업 불필요. ES 데이터는 es_data 볼륨에 유지됨.

                                    [참조 DB]
                                    읽기: MySQL consultation_results, consultation_raw_texts
                                    읽기: MongoDB consultation_summary
                                    쓰기: Elasticsearch consult-index
                                    """),
                    new Tag().name("② ES 사전 관리")
                            .description("""
                                    [언제] 단어사전 파일 수정 후 / 상담 데이터 기반 신규 키워드 추가 시
                                    [권한] 관리자(ROLE_ADMIN)만 접근 가능

                                    [사전 파일 종류]
                                    - synonyms.txt          : 검색 동의어 (갤폰→갤럭시, 번이→번호이동 등)
                                    - analysis_synonyms.txt : 분석 동의어 (인삿말, 응대품질, 감정 카테고리 등)
                                    - userdict.txt          : Nori 복합어 분해 방지 단어 목록
                                    - analysis_userdict.txt : 분석용 복합어 분해 방지 목록

                                    [실행 순서]
                                    1. (선택) GET  /admin/dictionary/extract → 추가될 키워드 미리보기
                                    2.        POST /admin/dictionary/update  → MongoDB 키워드 추출 후 userdict.txt 자동 추가
                                    3. (또는) POST /admin/dictionary/reload  → 파일 직접 수정 후 search-time 분석기만 리로드

                                    [참조 DB]
                                    읽기: MongoDB consultation_summary (summary.keywords, iam.matchKeyword)
                                    쓰기: userdict.txt 파일 (ES 볼륨 마운트 경로)
                                    """),
                    new Tag().name("③ ES 분석")
                            .description("""
                                    [언제] 응대품질 분석이 필요할 때
                                    [전제] POST /admin/es/sync 완료 필수 (실제 대화원문이 ES에 적재되어야 함)

                                    [두 분석 API의 차이]
                                    - analysis/quality  : 인덱싱 시 자동 계산된 hasGreeting/hasFarewell 필드 조회
                                                          실제 대화원문에서 상담사 발화만 추출하여 인삿말 감지
                                    - analysis/keywords : analysis_synonyms.txt 동의어 사전 적용 후 실질 내용 검색
                                                          응대 어근(안녕, 감사, 확인 등) 제거 후 매칭
                                    - es-test/analyze   : 사전이 정상 적용되는지 확인하는 검증 도구

                                    [사용 순서]
                                    1. POST /admin/es/sync                           → 데이터 적재 (전제조건)
                                    2. GET  /es-test/analyze                         → 사전 적용 확인 (선택)
                                    3. GET  /elasticsearch/consult/analysis/quality  → 응대품질 분석
                                    4. GET  /elasticsearch/consult/analysis/keywords → 키워드 패턴 분석

                                    [참조 DB]
                                    읽기: Elasticsearch consult-index
                                    (hasGreeting, hasFarewell, allText.analysis 서브필드)
                                    """),
                    new Tag().name("④ 상담 검색")
                            .description("""
                                    [언제] 상담 목록 검색 및 상세 조회 시
                                    [권한] 인증된 모든 사용자 접근 가능

                                    [검색 방식]
                                    - keyword 있을 때 : ES 키워드 검색(동의어 확장, 오타 허용) → consultId → MongoDB 조회
                                    - keyword 없을 때 : MongoDB 필터 조건만으로 직접 조회
                                    - 상세 조회       : ES 미사용, MySQL + MongoDB 병렬 조회

                                    [사용 순서]
                                    1. GET /summaries/suggest → 검색창 자동완성 (MongoDB 집계)
                                    2. GET /summaries         → 목록 검색 (keyword 입력 시 ES 사용)
                                    3. GET /summaries/{id}    → 상세 조회 (ES 미사용)

                                    [참조 DB]
                                    - GET /summaries (keyword) : ES consult-index → consultId → MongoDB consultation_summary
                                    - GET /summaries (필터만)  : MongoDB consultation_summary
                                    - GET /summaries/{id}      : MySQL + MongoDB 병렬 조회
                                    - GET /summaries/suggest   : MongoDB (summary.keywords, iam.matchKeyword 집계)
                                    """)
            ));
  }
}
