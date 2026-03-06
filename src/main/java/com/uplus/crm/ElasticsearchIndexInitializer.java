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

    private static final String INDEX_NAME = "consult-index";

    private final RestClient restClient;

    @PostConstruct
    public void createIndex() {
        try {
            // 인덱스 이미 존재하면 스킵
            Request headRequest = new Request("HEAD", "/" + INDEX_NAME);
            try {
                restClient.performRequest(headRequest);
                System.out.println("[ES] Index already exists, skipping: " + INDEX_NAME);
                return;
            } catch (Exception ignored) {
                // 404 응답 = 인덱스 없음 → 생성 진행
            }
            doCreate();
        } catch (Exception e) {
            System.err.println("[ES] Index creation failed (app startup continues): " + e.getMessage());
        }
    }

    /**
     * 기존 인덱스를 삭제하고 최신 settings/mappings로 재생성합니다.
     * 분석기 설정 변경 후 인덱스 재적용이 필요할 때 사용합니다.
     *
     * @return 결과 메시지
     */
    public String forceRecreate() throws Exception {
        // 1. 기존 인덱스 삭제 (없으면 무시)
        try {
            restClient.performRequest(new Request("DELETE", "/" + INDEX_NAME));
            System.out.println("[ES] Index deleted: " + INDEX_NAME);
        } catch (Exception ignored) {
            // 인덱스가 없는 경우 무시
        }
        // 2. 재생성
        doCreate();
        return "[ES] Index recreated successfully: " + INDEX_NAME;
    }

    private void doCreate() throws Exception {
        InputStream settingsIs = getClass().getClassLoader()
                .getResourceAsStream("elasticsearch/consult-settings.json");
        InputStream mappingsIs = getClass().getClassLoader()
                .getResourceAsStream("elasticsearch/consult-mapping.json");

        if (settingsIs == null || mappingsIs == null) {
            throw new IllegalStateException("Index config file not found in classpath.");
        }

        String settings = new String(settingsIs.readAllBytes());
        String mappings = new String(mappingsIs.readAllBytes());

        String body = """
                {
                  "settings": %s,
                  "mappings": %s
                }
                """.formatted(settings, mappings);

        Request request = new Request("PUT", "/" + INDEX_NAME);
        request.setJsonEntity(body);
        restClient.performRequest(request);
        System.out.println("[ES] Index created: " + INDEX_NAME);
    }
}
