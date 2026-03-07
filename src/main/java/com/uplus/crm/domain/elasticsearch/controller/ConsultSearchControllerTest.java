package com.uplus.crm.domain.elasticsearch.controller;

import com.uplus.crm.domain.elasticsearch.entity.ConsultDoc;
import com.uplus.crm.domain.elasticsearch.service.ConsultSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/elasticsearch/consult")
@RequiredArgsConstructor
public class ConsultSearchControllerTest {

    private final ConsultSearchService consultSearchService;

    @Operation(
        summary = "[Step 1] 테스트 데이터 적재",
        description = """
            Elasticsearch consult-index에 샘플 상담 데이터 18건을 저장합니다.
            검색 API를 테스트하기 전에 반드시 먼저 호출하세요.

            적재되는 데이터 목록:
            - C001: 갤폰/번이/미납 (NEGATIVE, HIGH, riskScore=75)
            - C002: 아폰/기변/선약 (POSITIVE, NORMAL, riskScore=10)
            - C003: 해지/kt번이/재약정 (NEGATIVE, URGENT, riskScore=90)
            - C004: 넷플/결합할인/5G (POSITIVE, LOW, riskScore=5)
            - C005: 폭언/반복민원 (NEGATIVE, URGENT, riskScore=95)
            - C006: 갤탭/유심/eSIM (NEUTRAL, NORMAL, riskScore=15)
            - C007: 셋탑/리모콘/이전설치 (NEUTRAL, NORMAL, riskScore=20)
            - C008: 디플/티빙/웨이브 (POSITIVE, LOW, riskScore=8)
            - C009: 와이파이/공유기/속도 (NEGATIVE, HIGH, riskScore=40)
            - C010: 로밍패스/해외로밍 (POSITIVE, NORMAL, riskScore=5)
            - C011: 피싱/사기의심 (NEGATIVE, URGENT, riskScore=88)
            - C012: 소비자원/위약금 (NEGATIVE, URGENT, riskScore=85)
            - C013: 폰케어/보험 (NEUTRAL, NORMAL, riskScore=25)
            - C014: VVIP/요금감면/분납 (POSITIVE, LOW, riskScore=5)
            - C015: 유쓰/너겟/청구서 (NEGATIVE, HIGH, riskScore=55)
            - C016: 인터넷전화/신규설치/5GB (POSITIVE, LOW, riskScore=5)
            - C017: 아이들나라/유튜브프리미엄 (POSITIVE, LOW, riskScore=5)
            - C018: 5G/LTE/통화품질 (NEGATIVE, HIGH, riskScore=45)
            """
    )
    @PostMapping("/test-data")
    public ResponseEntity<String> createTestData() {
        List<ConsultDoc> docs = List.of(

            // ── C001: 갤폰/번이/미납 ──────────────────────────────────────────
            ConsultDoc.builder()
                .id(UUID.randomUUID().toString())
                .iamIssue("갤럭시 S24 울트라 미납금 번호이동 제한")
                .iamAction("미납 납부 후 번호이동 가능 안내")
                .content("고객이 갤폰 미납금 있어서 번이 못 한다고 함. 미납요금 납부 후 진행 가능.")
                .allText("갤럭시 S24 울트라 미납금 번호이동 제한 미납요금 납부 안내")
                .customerName("김유플")
                .customerId("C001")
                .sentiment("NEGATIVE")
                .riskScore(75)
                .priority("HIGH")
                .phone("010-1234-5678")
                .createdAt(LocalDateTime.now())
                .build(),

            // ── C002: 아폰/기변/선약 ──────────────────────────────────────────
            ConsultDoc.builder()
                .id(UUID.randomUUID().toString())
                .iamIssue("아이폰 16 프로 기기변경 요청")
                .iamAction("아폰 기변 진행, 선택약정 24개월 안내 및 공시지원금 비교")
                .content("고객이 아폰으로 기변 원함. 선약 vs 공시 비교 안내. 아이폰 iphone 재고 확인.")
                .allText("아이폰 iphone 기기변경 기변 선택약정 선약 공시지원금")
                .customerName("이사과")
                .customerId("C002")
                .sentiment("POSITIVE")
                .riskScore(10)
                .priority("NORMAL")
                .phone("010-2345-6789")
                .createdAt(LocalDateTime.now().minusDays(1))
                .build(),

            // ── C003: 해지위험/kt/재약정 ─────────────────────────────────────
            ConsultDoc.builder()
                .id(UUID.randomUUID().toString())
                .iamIssue("서비스 해지 요청 - 타사 이동 검토 중")
                .iamAction("해지방어 혜택 제시, 재약정 3만원 할인 안내")
                .content("고객이 해지 원한다고 함. kt skt로 번이 고려 중. 해지위험 이탈위험 고객. 재약정 시 혜택 안내.")
                .allText("해지 서비스종료 중도해지 kt skt 번호이동 타사이동 해지위험 이탈위험 재약정 약정연장")
                .customerName("박해지")
                .customerId("C003")
                .sentiment("NEGATIVE")
                .riskScore(90)
                .priority("URGENT")
                .phone("010-3456-7890")
                .createdAt(LocalDateTime.now().minusDays(2))
                .build(),

            // ── C004: 넷플/결합할인/5G ───────────────────────────────────────
            ConsultDoc.builder()
                .id(UUID.randomUUID().toString())
                .iamIssue("넷플릭스 OTT 결합 요금제 문의")
                .iamAction("넷플 결합할인 5G 요금제 안내. 투게더 참쉬운결합 적용.")
                .content("고객이 넷플 보고 싶다고 함. 결합할인 가족결합 묶음할인 문의. 5G LTE 요금제 비교.")
                .allText("넷플릭스 netflix 넷플 결합할인 가족결합 투게더 참쉬운결합 5G LTE 요금제")
                .customerName("최유플")
                .customerId("C004")
                .sentiment("POSITIVE")
                .riskScore(5)
                .priority("LOW")
                .phone("010-4567-8901")
                .createdAt(LocalDateTime.now().minusDays(3))
                .build(),

            // ── C005: 폭언/반복민원/블랙컨슈머 ──────────────────────────────
            ConsultDoc.builder()
                .id(UUID.randomUUID().toString())
                .iamIssue("폭언 욕설 고객 응대 - 반복 민원 고질 민원")
                .iamAction("감정노동 보호 절차 진행, 상담 종료")
                .content("고객이 욕설 및 폭언 갑질 언어폭력 지속. 블랙컨슈머 고질민원 상습민원 이력 있음.")
                .allText("폭언 욕설 갑질 언어폭력 반복민원 고질민원 상습민원 블랙컨슈머")
                .customerName("홍민원")
                .customerId("C005")
                .sentiment("NEGATIVE")
                .riskScore(95)
                .priority("URGENT")
                .phone("010-5678-9012")
                .createdAt(LocalDateTime.now().minusDays(4))
                .build(),

            // ── C006: 갤탭/유심/eSIM ─────────────────────────────────────────
            ConsultDoc.builder()
                .id(UUID.randomUUID().toString())
                .iamIssue("갤럭시탭 신규 개통 - eSIM 유심 설정 문의")
                .iamAction("갤탭 eSIM 설정 원격 안내. 나노유심 물리유심 교체 불필요 설명.")
                .content("고객이 갤탭 새로 사서 esim 설정 못 하겠다고 함. 유심 usim 가입자식별모듈 문의.")
                .allText("갤럭시탭 갤탭 galaxy eSIM 유심 usim 나노유심 물리유심 가입자식별모듈")
                .customerName("정탭순")
                .customerId("C006")
                .sentiment("NEUTRAL")
                .riskScore(15)
                .priority("NORMAL")
                .phone("010-6789-0123")
                .createdAt(LocalDateTime.now().minusDays(5))
                .build(),

            // ── C007: 셋탑/리모콘/이전설치 ───────────────────────────────────
            ConsultDoc.builder()
                .id(UUID.randomUUID().toString())
                .iamIssue("이사 후 셋톱박스 이전설치 및 리모컨 불량")
                .iamAction("이사설치 일정 예약. 리모콘 리모컨 교체 신청 접수.")
                .content("고객이 이사해서 셋탑 이전설치 원함. 셋탑박스 세톱박스 리모컨 리모콘 고장으로 교체 요청.")
                .allText("셋톱박스 셋탑 세톱박스 티비박스 리모컨 리모콘 이전설치 이사설치 설치변경")
                .customerName("오이사")
                .customerId("C007")
                .sentiment("NEUTRAL")
                .riskScore(20)
                .priority("NORMAL")
                .phone("010-7890-1234")
                .createdAt(LocalDateTime.now().minusDays(6))
                .build(),

            // ── C008: 디플/티빙/웨이브 OTT ────────────────────────────────────
            ConsultDoc.builder()
                .id(UUID.randomUUID().toString())
                .iamIssue("디즈니플러스 티빙 웨이브 OTT 부가서비스 문의")
                .iamAction("디플 티빙플러스 웨이브플러스 결합 요금제 안내")
                .content("고객이 디플 구독 중인데 티빙 웨이브도 보고 싶다고 함. OTT 결합상품 설명.")
                .allText("디즈니플러스 디플 티빙 tving 티빙플러스 웨이브 wavve 웨이브플러스 OTT 부가서비스")
                .customerName("윤OTT")
                .customerId("C008")
                .sentiment("POSITIVE")
                .riskScore(8)
                .priority("LOW")
                .phone("010-8901-2345")
                .createdAt(LocalDateTime.now().minusDays(7))
                .build(),

            // ── C009: 와이파이/공유기/속도 ────────────────────────────────────
            ConsultDoc.builder()
                .id(UUID.randomUUID().toString())
                .iamIssue("와이파이 속도 저하 - 공유기 장애 문의")
                .iamAction("원격 점검 후 공유기 라우터 교체 안내. 기가인터넷 1G 속도 정상 확인.")
                .content("고객이 와이파이 무선인터넷 느리다고 함. 공유기 라우터 루터 ap 점검. 기가슬림 500m 1G 속도 측정.")
                .allText("와이파이 wifi 무선인터넷 공유기 라우터 루터 ap 기가인터넷 기가슬림 100m 500m 1g 속도")
                .customerName("강와이파이")
                .customerId("C009")
                .sentiment("NEGATIVE")
                .riskScore(40)
                .priority("HIGH")
                .phone("010-9012-3456")
                .createdAt(LocalDateTime.now().minusDays(8))
                .build(),

            // ── C010: 로밍패스/해외로밍 ──────────────────────────────────────
            ConsultDoc.builder()
                .id(UUID.randomUUID().toString())
                .iamIssue("해외 출장 로밍 패스 신청")
                .iamAction("로밍패스 해외로밍 데이터로밍 유럽 요금제 안내")
                .content("고객이 유럽 출장 예정. 로밍 데이터로밍 해외로밍 로밍패스 신청 원함. 일 1만원 무제한 안내.")
                .allText("로밍패스 해외로밍 데이터로밍 유럽로밍 유럽 출장 일 무제한")
                .customerName("임해외")
                .customerId("C010")
                .sentiment("POSITIVE")
                .riskScore(5)
                .priority("NORMAL")
                .phone("010-0123-4567")
                .createdAt(LocalDateTime.now().minusDays(9))
                .build(),

            // ── C011: 피싱/사기의심 ────────────────────────────────────────────
            ConsultDoc.builder()
                .id(UUID.randomUUID().toString())
                .iamIssue("보이스피싱 의심 - 사기 부정사용 신고")
                .iamAction("피싱 사기의심 계정 일시 정지 및 보안 조치 진행")
                .content("고객이 스미싱 보이스피싱 문자 받았다고 함. 부정사용 사기 의심. 피싱피해 피싱해킹 신고 접수.")
                .allText("피싱 스미싱 보이스피싱 피싱해킹 사기 부정사용 사기의심 피싱피해")
                .customerName("서피싱")
                .customerId("C011")
                .sentiment("NEGATIVE")
                .riskScore(88)
                .priority("URGENT")
                .phone("010-1122-3344")
                .createdAt(LocalDateTime.now().minusDays(10))
                .build(),

            // ── C012: 소비자원/방통위/위약금 ──────────────────────────────────
            ConsultDoc.builder()
                .id(UUID.randomUUID().toString())
                .iamIssue("소비자원 방통위 민원 예고 - 위약금 분쟁")
                .iamAction("위약금 할인반환금 산정 근거 상세 안내. 분쟁 예방 조치.")
                .content("고객이 소비자원 방통위에 민원 넣겠다고 함. 위약금 해지비용 해지위약금 환불 요구.")
                .allText("소비자원 방통위 위약금 할인반환금 해지비용 해지위약금 환불 분쟁 민원")
                .customerName("조소비자")
                .customerId("C012")
                .sentiment("NEGATIVE")
                .riskScore(85)
                .priority("URGENT")
                .phone("010-2233-4455")
                .createdAt(LocalDateTime.now().minusDays(11))
                .build(),

            // ── C013: 유플러스폰케어/보험 ─────────────────────────────────────
            ConsultDoc.builder()
                .id(UUID.randomUUID().toString())
                .iamIssue("갤폰 파손 - 유플러스폰케어 보험 청구")
                .iamAction("폰케어프리미엄 U+폰케어 파손 접수 및 수리비 안내")
                .content("고객이 갤폰 액정 깨져서 폰케어 보험 청구 원함. 휴대폰보험 스마트폰보험 유플러스폰케어 접수.")
                .allText("유플러스폰케어 폰케어프리미엄 U+폰케어 휴대폰보험 스마트폰보험 파손 액정 수리")
                .customerName("권파손")
                .customerId("C013")
                .sentiment("NEUTRAL")
                .riskScore(25)
                .priority("NORMAL")
                .phone("010-3344-5566")
                .createdAt(LocalDateTime.now().minusDays(12))
                .build(),

            // ── C014: VVIP/다이아몬드/요금감면 ───────────────────────────────
            ConsultDoc.builder()
                .id(UUID.randomUUID().toString())
                .iamIssue("VVIP 다이아몬드 고객 요금감면 및 분할납부 요청")
                .iamAction("VIP 우수고객 장기할인 요금감면 장애인감면 적용. 분납 분할납부 12개월 안내.")
                .content("VVIP 브이브이아이피 다이아몬드 우수고객 고객. 요금감면 할인 감면 요청. 분할납부 나눠내기 분납 안내.")
                .allText("vvip 브이브이아이피 vip 브이아이피 diamond 다이아몬드 우수고객 요금감면 할인 감면 분할납부 분납")
                .customerName("나VVIP")
                .customerId("C014")
                .sentiment("POSITIVE")
                .riskScore(5)
                .priority("LOW")
                .phone("010-4455-6677")
                .createdAt(LocalDateTime.now().minusDays(13))
                .build(),

            // ── C015: 유쓰/너겟/청구서 ────────────────────────────────────────
            ConsultDoc.builder()
                .id(UUID.randomUUID().toString())
                .iamIssue("유쓰 너겟 청구서 이중청구 오류")
                .iamAction("청구서 고지서 명세서 재발행. 이중청구 소급적용 환불 처리.")
                .content("고객이 유쓰 youth 유스 너겟 nugget 요금제 청구서 명세서 고지서 요금내역서 확인 중 이중청구 발견.")
                .allText("유쓰 youth 유스 너겟 nugget 청구서 고지서 명세서 요금내역서 이중청구 소급적용")
                .customerName("하유쓰")
                .customerId("C015")
                .sentiment("NEGATIVE")
                .riskScore(55)
                .priority("HIGH")
                .phone("010-5566-7788")
                .createdAt(LocalDateTime.now().minusDays(14))
                .build(),

            // ── C016: 인터넷전화/신규설치/5GB ─────────────────────────────────
            ConsultDoc.builder()
                .id(UUID.randomUUID().toString())
                .iamIssue("인터넷전화 홈전화 신규설치 및 데이터 5GB 추가")
                .iamAction("인터넷전화 유선전화 홈전화 신규설치 예약. 데이터쉐어링 5GB 추가.")
                .content("고객이 홈전화 인터넷전화 유선전화 신규설치 원함. 데이터 5GB 오기가 5gb 추가 문의.")
                .allText("인터넷전화 홈전화 유선전화 신규설치 첫설치 데이터쉐어링 5gb 오기가 5g")
                .customerName("문인터넷")
                .customerId("C016")
                .sentiment("POSITIVE")
                .riskScore(5)
                .priority("LOW")
                .phone("010-6677-8899")
                .createdAt(LocalDateTime.now().minusDays(15))
                .build(),

            // ── C017: 아이들나라/유튜브프리미엄/요금제 ───────────────────────
            ConsultDoc.builder()
                .id(UUID.randomUUID().toString())
                .iamIssue("아이들나라 유튜브프리미엄 결합 요금제 문의")
                .iamAction("아이들나라 아이들네이션 유튜브프리미어 유튜브유료 결합상품 안내")
                .content("고객이 아이들나라 유튜브프리미엄 유튜브유료 결합 원함. 요금상품 요금종류 비교.")
                .allText("아이들나라 아이들네이션 유튜브프리미엄 유튜브유료 결합할인 요금제 요금상품 요금종류")
                .customerName("양아이")
                .customerId("C017")
                .sentiment("POSITIVE")
                .riskScore(5)
                .priority("LOW")
                .phone("010-7788-9900")
                .createdAt(LocalDateTime.now().minusDays(16))
                .build(),

            // ── C018: LTE/5G 속도불만/통화품질 ──────────────────────────────
            ConsultDoc.builder()
                .id(UUID.randomUUID().toString())
                .iamIssue("5G LTE 통화품질 속도 불만 - 네트워크 장애 의심")
                .iamAction("네트워크재설정 원격점검 안내. 5G 5세대 5g네트워크 기지국 점검 요청.")
                .content("고객이 5G 5세대 lte 4g 통화품질 속도 불만. 와이파이6 wifi 연결 문제. APN IMEI 초기화 안내.")
                .allText("5g 5세대 5g네트워크 lte 4g 통화품질 속도 와이파이 wifi 무선인터넷 APN IMEI 원격점검")
                .customerName("류속도")
                .customerId("C018")
                .sentiment("NEGATIVE")
                .riskScore(45)
                .priority("HIGH")
                .phone("010-8899-0011")
                .createdAt(LocalDateTime.now().minusDays(17))
                .build()
        );

        docs.forEach(consultSearchService::saveConsultation);
        return ResponseEntity.ok("테스트 데이터 " + docs.size() + "건 저장 완료!");
    }

    // ==================== [분석 전용] 응대품질 분석 API ====================

    @Operation(
        summary = "[분석] 인삿말 여부 기반 응대품질 조회",
        description = """
            hasGreeting·hasFarewell 필드로 응대품질 미달 상담을 필터링합니다.
            저장 시 자동 감지되는 필드입니다.

            **감지 패턴**
            - 인사말: 안녕하세요 / 안녕하십니까 / 반갑습니다 / 좋은 아침
            - 마무리: 감사합니다 / 감사드립니다 / 수고하세요 / 고맙습니다 / 안녕히 계세요 등

            **활용 예시**
            - `hasGreeting=false`  → 인사말 없이 시작한 상담 (품질 미달 후보)
            - `hasFarewell=false`  → 마무리 인사 없이 종료한 상담 (품질 미달 후보)
            - 둘 다 false          → 인사 완전 미포함 상담 (최우선 관리 대상)
            - 둘 다 생략           → 전체 상담 반환 (riskScore 내림차순)
            """
    )
    @GetMapping("/analysis/quality")
    public ResponseEntity<List<ConsultDoc>> analyzeQuality(
            @Parameter(description = "인사말 포함 여부 필터 (true/false, 생략 시 무조건)", example = "false")
            @RequestParam(required = false) Boolean hasGreeting,
            @Parameter(description = "마무리 인사 포함 여부 필터 (true/false, 생략 시 무조건)", example = "false")
            @RequestParam(required = false) Boolean hasFarewell,
            @Parameter(description = "페이지 번호", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(
                consultSearchService.searchByGreetingFlag(hasGreeting, hasFarewell, page, size));
    }

    @Operation(
        summary = "[분석] 분석용 키워드 검색 (인삿말·응대 어근 제거)",
        description = """
            `allText.analysis` 서브필드를 대상으로 검색합니다.
            검색용 `allText`와 달리 다음 토큰이 분석 단계에서 제거됩니다.

            **제거 대상**
            - 인삿말·마무리말 어근: 안녕, 반갑, 감사, 죄송, 수고, 실례
            - 일반 응대 어근: 확인, 안내, 처리, 연결, 진행, 설명 (모든 상담에 공통 출현)
            - 검색 불용어 전체 포함

            **용도**
            - 실제 상담 내용(issue/action)에 집중한 키워드 분석
            - 응대 품질 지표 추출
            - 유사 상담 군집 분석 (카테고리 미분류 상담 탐지)

            **분석기**: `korean_analysis_index_analyzer` / `korean_analysis_search_analyzer`
            (decompound_mode: discard + analysis 전용 사전)
            """
    )
    @GetMapping("/analysis/keywords")
    public ResponseEntity<List<ConsultDoc>> analyzeKeywords(
            @Parameter(description = "분석할 키워드", example = "미납 번호이동")
            @RequestParam String keyword,
            @Parameter(description = "페이지 번호", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(
                consultSearchService.searchByAnalysisKeyword(keyword, page, size));
    }
}
