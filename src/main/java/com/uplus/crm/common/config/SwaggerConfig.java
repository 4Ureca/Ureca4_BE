package com.uplus.crm.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
            .addSecurityItem(securityRequirement) // 모든 API 옆에 자물쇠 아이콘 표시
            .components(components);
  }
}