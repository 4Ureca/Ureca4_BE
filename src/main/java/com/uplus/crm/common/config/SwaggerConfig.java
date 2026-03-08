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
                                    최초 1회 또는 사전 변경 시에만 사용합니다.
                                    평소 앱 재시작 시에는 호출할 필요가 없습니다.

                                    [실행 순서]
                                    1. (사전 변경 시) POST /es-test/recreate-index  → 인덱스 재생성
                                    2-A. POST /admin/es/sync                        → 실제 대화원문 데이터 적재
                                    2-B. POST /elasticsearch/consult/test-data      → 더미 데이터 적재 (테스트용)
                                    """),
                    new Tag().name("② ES 사전 관리")
                            .description("""
                                    analysis_synonyms.txt 등 사전 파일을 수정한 후 사용합니다.
                                    MongoDB 상담 데이터에서 신규 키워드를 자동 추출하여 userdict.txt에 추가합니다.
                                    """),
                    new Tag().name("③ ES 분석")
                            .description("""
                                    인덱싱 시점에 미리 계산·저장된 분석값을 조회합니다.
                                    (hasGreeting/hasFarewell, allText.analysis 서브필드)

                                    ✅ POST /admin/es/sync 로 실제 대화원문을 적재한 후 호출해야 정확한 결과가 나옵니다.
                                    """),
                    new Tag().name("④ 상담 검색")
                            .description("""
                                    ES를 검색 엔진으로 사용하는 단순 검색 API입니다.
                                    키워드 입력 → ES 검색 → consultId 추출 → MongoDB/RDB 상세 데이터 반환.

                                    [사용 순서]
                                    1. GET /summaries/suggest   → 키워드 자동완성
                                    2. GET /summaries           → 목록 검색 (keyword + 필터)
                                    3. GET /summaries/{id}      → 상세 조회
                                    """)
            ));
  }
}