package com.uplus.crm.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

  @Bean
  public OpenAPI openAPI() {
    return new OpenAPI()
        .info(new Info()
            .title("고객 상담 기록 관리 서비스 API")
            .description("고객 상담 기록 관리 서비스의 API입니다.")
            .version("1.0.0"));
  }
}
