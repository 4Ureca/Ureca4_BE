package com.uplus.crm.domain.elasticsearch.controller;

import com.uplus.crm.domain.elasticsearch.entity.ConsultDoc;
import com.uplus.crm.domain.elasticsearch.service.ConsultSearchService;
import com.uplus.crm.domain.summary.document.ConsultationSummary;
import com.uplus.crm.domain.summary.repository.SummaryRepository;
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
    private final SummaryRepository summaryRepository;

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
        // consultId: ES ↔ MongoDB 연결 키 (1001~1018)
        List<ConsultDoc> docs = List.of(

            // ── C001: 갤폰/번이/미납 ──────────────────────────────────────────
            ConsultDoc.builder()
                .id(UUID.randomUUID().toString())
                .consultId(1001L)
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
                .consultId(1002L)
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
                .consultId(1003L)
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
                .consultId(1004L)
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
                .consultId(1005L)
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
                .consultId(1006L)
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
                .consultId(1007L)
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
                .consultId(1008L)
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
                .consultId(1009L)
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
                .consultId(1010L)
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
                .consultId(1011L)
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
                .consultId(1012L)
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
                .consultId(1013L)
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
                .consultId(1014L)
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
                .consultId(1015L)
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
                .consultId(1016L)
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
                .consultId(1017L)
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
                .consultId(1018L)
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
                .build(),

            // ════════════════════════════════════════════════════════════════
            // 추가 데이터 C019~C100 (82건) — 카테고리별 다양한 상담 시나리오
            // ════════════════════════════════════════════════════════════════

            // ── 요금/납부 ────────────────────────────────────────────────────
            ConsultDoc.builder().id(UUID.randomUUID().toString()).consultId(1019L)
                .iamIssue("5G 요금제 변경 요청 - 더 저렴한 요금제로 다운그레이드")
                .iamAction("5G 슬림+ 요금제 안내 및 변경 처리").content("고객이 요금이 너무 비싸다며 더 저렴한 5G 요금제를 원함. 5G 슬림플러스 47000원 안내.")
                .allText("5g 슬림플러스 요금제 다운그레이드 요금인하 저렴한 요금제").customerName("김요금").customerId("C019").sentiment("NEUTRAL").riskScore(15).priority("NORMAL").phone("010-1001-0001").createdAt(LocalDateTime.now().minusDays(18)).build(),

            ConsultDoc.builder().id(UUID.randomUUID().toString()).consultId(1020L)
                .iamIssue("데이터 쉐어링 추가 회선 등록 문의")
                .iamAction("데이터 쉐어링 태블릿 회선 추가 등록 처리").content("고객이 본인 태블릿에 데이터 쉐어링 추가하고 싶다고 함. 데이터선물하기와 차이 안내.")
                .allText("데이터쉐어링 태블릿 데이터나누기 데이터공유 추가회선").customerName("이태블릿").customerId("C020").sentiment("POSITIVE").riskScore(5).priority("LOW").phone("010-1002-0002").createdAt(LocalDateTime.now().minusDays(20)).build(),

            ConsultDoc.builder().id(UUID.randomUUID().toString()).consultId(1021L)
                .iamIssue("자동이체 카드 변경 요청")
                .iamAction("자동이체 신용카드 변경 처리 완료").content("고객이 기존 국민카드에서 신한카드로 자동이체 변경 원함. 변경 완료 안내.")
                .allText("자동이체 카드변경 신한카드 국민카드 납부방법변경").customerName("박카드").customerId("C021").sentiment("POSITIVE").riskScore(3).priority("LOW").phone("010-1003-0003").createdAt(LocalDateTime.now().minusDays(22)).build(),

            ConsultDoc.builder().id(UUID.randomUUID().toString()).consultId(1022L)
                .iamIssue("청구서 이중청구 항목 이상 문의")
                .iamAction("청구서 내역 확인 후 오류 소급 환불 처리").content("고객이 이번 달 청구서에 같은 항목이 두 번 청구되어 있다고 문의. 이중청구 확인 후 환불.")
                .allText("청구서 이중청구 환불 청구오류 요금이의 소급적용").customerName("최청구").customerId("C022").sentiment("NEGATIVE").riskScore(40).priority("HIGH").phone("010-1004-0004").createdAt(LocalDateTime.now().minusDays(25)).build(),

            ConsultDoc.builder().id(UUID.randomUUID().toString()).consultId(1023L)
                .iamIssue("장애인 요금 감면 신청")
                .iamAction("장애인 감면 서류 안내 및 신청 접수").content("고객이 장애인 등록증 있고 요금 감면 받고 싶다고 함. 신체대상 감면 월 11000원 안내.")
                .allText("장애인감면 기초생활수급자 요금감면 할인 감면신청").customerName("정감면").customerId("C023").sentiment("POSITIVE").riskScore(5).priority("LOW").phone("010-1005-0005").createdAt(LocalDateTime.now().minusDays(28)).build(),

            ConsultDoc.builder().id(UUID.randomUUID().toString()).consultId(1024L)
                .iamIssue("미납 요금 납부 방법 문의 - 장기 미납")
                .iamAction("미납 요금 분할 납부 12개월 신청 처리").content("고객이 3개월치 미납 요금 한번에 내기 어렵다고 함. 분납 신청 및 서비스 이용 유지 안내.")
                .allText("미납요금 장기미납 분납 분할납부 납부유예 요금밀림").customerName("강미납").customerId("C024").sentiment("NEGATIVE").riskScore(55).priority("HIGH").phone("010-1006-0006").createdAt(LocalDateTime.now().minusDays(30)).build(),

            ConsultDoc.builder().id(UUID.randomUUID().toString()).consultId(1025L)
                .iamIssue("결합 할인 U+투게더 가입 문의")
                .iamAction("U+투게더 가족결합 조건 안내 및 신청 접수").content("고객이 가족이랑 결합해서 할인받고 싶다고 함. 투게더 결합할인 조건 및 장기고객할인 추가 안내.")
                .allText("U+투게더 가족결합 결합할인 투게더 참쉬운결합 장기고객").customerName("오결합").customerId("C025").sentiment("POSITIVE").riskScore(5).priority("LOW").phone("010-1007-0007").createdAt(LocalDateTime.now().minusDays(32)).build(),

            ConsultDoc.builder().id(UUID.randomUUID().toString()).consultId(1026L)
                .iamIssue("요금 명세서 항목 상세 문의")
                .iamAction("요금 명세서 항목별 상세 설명 안내").content("고객이 청구서 고지서에 모르는 항목이 있다고 함. 요금내역서 항목 하나씩 설명 완료.")
                .allText("요금명세서 청구서 고지서 요금내역서 항목 기본료 부가서비스").customerName("윤명세").customerId("C026").sentiment("NEUTRAL").riskScore(10).priority("NORMAL").phone("010-1008-0008").createdAt(LocalDateTime.now().minusDays(35)).build(),

            ConsultDoc.builder().id(UUID.randomUUID().toString()).consultId(1027L)
                .iamIssue("LTE 요금제에서 5G 요금제 업그레이드 문의")
                .iamAction("5G 프리미어 에센셜 업그레이드 처리").content("고객이 LTE에서 5G로 올리고 싶다고 함. 5G 프리미어 에센셜 85000원 혜택 안내 및 변경.")
                .allText("lte 5g 업그레이드 요금제변경 5g프리미어에센셜 혜택").customerName("서업그").customerId("C027").sentiment("POSITIVE").riskScore(5).priority("LOW").phone("010-1009-0009").createdAt(LocalDateTime.now().minusDays(38)).build(),

            ConsultDoc.builder().id(UUID.randomUUID().toString()).consultId(1028L)
                .iamIssue("로밍 서비스 요금 이의 제기")
                .iamAction("로밍 요금 내역 확인 및 일부 소급 처리").content("고객이 일본 여행 후 로밍 요금이 예상보다 많이 나왔다고 이의 제기. 요금 산정 기준 설명 및 일부 환불.")
                .allText("로밍 해외로밍 로밍요금 이의제기 일본 요금환불 소급").customerName("임로밍").customerId("C028").sentiment("NEGATIVE").riskScore(35).priority("HIGH").phone("010-1010-0010").createdAt(LocalDateTime.now().minusDays(40)).build(),

            // ── 해지/재약정 ──────────────────────────────────────────────────
            ConsultDoc.builder().id(UUID.randomUUID().toString()).consultId(1029L)
                .iamIssue("번호이동 해지 요청 - SKT로 이동 예정")
                .iamAction("번호이동 해지 방어 혜택 제시 후 잔류 결정").content("고객이 SKT skt로 번이 가겠다고 해지 요청. 특별 재약정 혜택 안내 후 잔류 결정.")
                .allText("번호이동 skt 해지 해지방어 잔류 재약정 타사이동 번이").customerName("문번이").customerId("C029").sentiment("NEGATIVE").riskScore(80).priority("URGENT").phone("010-2001-0001").createdAt(LocalDateTime.now().minusDays(42)).build(),

            ConsultDoc.builder().id(UUID.randomUUID().toString()).consultId(1030L)
                .iamIssue("약정 만료 재약정 혜택 안내 요청")
                .iamAction("재약정 3만원 할인 혜택 안내 및 신청 처리").content("고객이 약정 만료 알림 받고 재약정 혜택 물어봄. 약정갱신 3만원 추가 할인 안내 및 신청.")
                .allText("재약정 약정연장 약정만료 약정갱신 혜택 할인").customerName("양약정").customerId("C030").sentiment("POSITIVE").riskScore(8).priority("LOW").phone("010-2002-0002").createdAt(LocalDateTime.now().minusDays(44)).build(),

            ConsultDoc.builder().id(UUID.randomUUID().toString()).consultId(1031L)
                .iamIssue("인터넷 해지 요청 - 이사로 인한 해지")
                .iamAction("이전 설치 안내로 해지 철회 및 이전 예약").content("고객이 이사한다고 인터넷 해지 원함. 이사이전설치 서비스 안내로 해지 방어 성공.")
                .allText("인터넷해지 이전설치 이사 해지방어 이사설치 설치변경").customerName("조이사").customerId("C031").sentiment("POSITIVE").riskScore(30).priority("NORMAL").phone("010-2003-0003").createdAt(LocalDateTime.now().minusDays(46)).build(),

            ConsultDoc.builder().id(UUID.randomUUID().toString()).consultId(1032L)
                .iamIssue("위약금 면제 요청 - 중도해지")
                .iamAction("위약금 할인반환금 산정 기준 상세 안내").content("고객이 약정 중간에 해지하면 위약금 면제해줄 수 없냐고 함. 위약금 산정 기준 안내.")
                .allText("위약금 면제 중도해지 할인반환금 해지비용 해지위약금").customerName("권위약").customerId("C032").sentiment("NEGATIVE").riskScore(50).priority("HIGH").phone("010-2004-0004").createdAt(LocalDateTime.now().minusDays(48)).build(),

            ConsultDoc.builder().id(UUID.randomUUID().toString()).consultId(1033L)
                .iamIssue("KT 번호이동 후 재가입 문의")
                .iamAction("U+ 재가입 혜택 안내 및 신규 개통 처리").content("고객이 예전에 kt로 번이 갔다가 다시 유플러스로 오고 싶다고 함. 재가입 혜택 안내.")
                .allText("kt 재가입 신규개통 번호이동 유플러스 혜택 기기변경").customerName("나재가").customerId("C033").sentiment("POSITIVE").riskScore(5).priority("LOW").phone("010-2005-0005").createdAt(LocalDateTime.now().minusDays(50)).build(),

            ConsultDoc.builder().id(UUID.randomUUID().toString()).consultId(1034L)
                .iamIssue("이용 정지 상태 문의 - 미납으로 정지")
                .iamAction("미납 요금 납부 안내 후 즉시 이용 정지 해제").content("고객이 갑자기 전화가 안된다고 문의. 미납으로 직권정지 상태. 납부 후 즉시 해제 처리.")
                .allText("이용정지 직권정지 미납 서비스정지 일시정지해제 납부유예").customerName("하정지").customerId("C034").sentiment("NEGATIVE").riskScore(60).priority("HIGH").phone("010-2006-0006").createdAt(LocalDateTime.now().minusDays(52)).build(),

            ConsultDoc.builder().id(UUID.randomUUID().toString()).consultId(1035L)
                .iamIssue("해지 방어 - 넷플릭스 결합 혜택 제시")
                .iamAction("넷플릭스 결합 혜택 제시로 해지 방어 성공").content("고객이 해지하겠다고 했으나 넷플릭스 OTT 결합 혜택 제시 후 잔류. 해지방어 성공.")
                .allText("해지방어 넷플릭스 OTT 결합혜택 잔류 해지예방").customerName("류해지").customerId("C035").sentiment("POSITIVE").riskScore(70).priority("URGENT").phone("010-2007-0007").createdAt(LocalDateTime.now().minusDays(54)).build(),

            ConsultDoc.builder().id(UUID.randomUUID().toString()).consultId(1036L)
                .iamIssue("알뜰폰 MVNO로의 전환 문의")
                .iamAction("알뜰폰과 비교한 U+ 혜택 설명으로 전환 방지").content("고객이 알뜰폰 mvno가 더 저렴하지 않냐고 비교 문의. U+투게더 결합 시 더 저렴함을 안내.")
                .allText("알뜰폰 MVNO 알뜰통신사 저가통신사 비교 제2통신사 전환방지").customerName("서알뜰").customerId("C036").sentiment("NEUTRAL").riskScore(40).priority("NORMAL").phone("010-2008-0008").createdAt(LocalDateTime.now().minusDays(56)).build(),

            ConsultDoc.builder().id(UUID.randomUUID().toString()).consultId(1037L)
                .iamIssue("장기 재약정 할인율 문의")
                .iamAction("5년 이상 장기고객 추가 할인 안내 및 적용").content("고객이 10년 넘게 쓰고 있는데 장기 고객 혜택 없냐고 문의. 장기고객할인 추가 적용.")
                .allText("장기고객 장기할인 재약정할인 장기사용고객 충성고객 혜택").customerName("문장기").customerId("C037").sentiment("POSITIVE").riskScore(5).priority("LOW").phone("010-2009-0009").createdAt(LocalDateTime.now().minusDays(58)).build(),

            ConsultDoc.builder().id(UUID.randomUUID().toString()).consultId(1038L)
                .iamIssue("해지 후 번호 유지 가능 여부 문의")
                .iamAction("번호 유지 조건 안내 및 번호이동 절차 설명").content("고객이 번호는 유지하면서 다른 통신사로 가고 싶다고 함. 번호이동 절차 상세 안내.")
                .allText("번호유지 번호이동 번이 통신사변경 타사이동 번호포팅").customerName("임번포").customerId("C038").sentiment("NEUTRAL").riskScore(75).priority("URGENT").phone("010-2010-0010").createdAt(LocalDateTime.now().minusDays(60)).build(),

            // ── 기기변경 ─────────────────────────────────────────────────────
            ConsultDoc.builder().id(UUID.randomUUID().toString()).consultId(1039L)
                .iamIssue("갤럭시 S25 울트라 사전예약 문의")
                .iamAction("갤럭시 S25 공시지원금 및 선택약정 비교 안내").content("고객이 갤S25 울트라 사전예약하고 싶다고 함. 공시지원금 vs 선약 비교 설명.")
                .allText("갤럭시 갤폰 s25 울트라 사전예약 공시지원금 선택약정 기기변경").customerName("조갤럭").customerId("C039").sentiment("POSITIVE").riskScore(5).priority("LOW").phone("010-3001-0001").createdAt(LocalDateTime.now().minusDays(62)).build(),

            ConsultDoc.builder().id(UUID.randomUUID().toString()).consultId(1040L)
                .iamIssue("아이폰 15 기기변경 - 할부원금 조회")
                .iamAction("아이폰 15 잔여 할부금 확인 및 완납 처리").content("고객이 아이폰 iphone 15로 기변하고 싶은데 현재 기기 잔여 할부금 얼마인지 문의. 할부원금 조회 후 완납.")
                .allText("아이폰 iphone 15 기기변경 기변 잔여할부금 할부원금 완납").customerName("박아폰").customerId("C040").sentiment("NEUTRAL").riskScore(10).priority("NORMAL").phone("010-3002-0002").createdAt(LocalDateTime.now().minusDays(64)).build(),

            ConsultDoc.builder().id(UUID.randomUUID().toString()).consultId(1041L)
                .iamIssue("중고폰 자급제 개통 방법 문의")
                .iamAction("자급제 공기계 유심 개통 절차 안내").content("고객이 중고폰 자급제로 구매했는데 유심 개통 어떻게 하냐고 문의. 유심 발급 및 개통 절차 안내.")
                .allText("자급제 공기계 중고폰 유심개통 유심발급 나노유심 esim").customerName("최자급").customerId("C041").sentiment("POSITIVE").riskScore(5).priority("LOW").phone("010-3003-0003").createdAt(LocalDateTime.now().minusDays(66)).build(),

            ConsultDoc.builder().id(UUID.randomUUID().toString()).consultId(1042L)
                .iamIssue("단말기 할부 이자 면제 요청")
                .iamAction("할부 이자 면제 조건 안내 - 해당 없음 설명").content("고객이 단말기 할부 이자 면제해달라고 함. 무이자 할부는 특정 카드만 가능하다고 안내.")
                .allText("할부이자 단말기할부 무이자할부 할부원금 할부이자면제").customerName("오할부").customerId("C042").sentiment("NEGATIVE").riskScore(25).priority("NORMAL").phone("010-3004-0004").createdAt(LocalDateTime.now().minusDays(68)).build(),

            ConsultDoc.builder().id(UUID.randomUUID().toString()).consultId(1043L)
                .iamIssue("갤럭시 탭 신규 개통 - 데이터 요금제 선택")
                .iamAction("갤탭 전용 데이터 쉐어링 요금제 안내").content("고객이 갤탭 새로 구매해서 데이터 연결하고 싶다고 함. 데이터쉐어링 요금제 안내.")
                .allText("갤럭시탭 갤탭 데이터쉐어링 태블릿 신규개통 요금제").customerName("윤갤탭").customerId("C043").sentiment("POSITIVE").riskScore(5).priority("LOW").phone("010-3005-0005").createdAt(LocalDateTime.now().minusDays(70)).build(),

            ConsultDoc.builder().id(UUID.randomUUID().toString()).consultId(1044L)
                .iamIssue("기기변경 후 데이터 이전 문의")
                .iamAction("기기변경 후 기기 간 데이터 이전 방법 안내").content("고객이 새 폰으로 기변 후 카카오톡 사진 연락처 다 옮기고 싶다고 문의. 이전 방법 안내.")
                .allText("기기변경 기변 데이터이전 카카오톡 연락처 사진이전").customerName("서이전").customerId("C044").sentiment("POSITIVE").riskScore(5).priority("LOW").phone("010-3006-0006").createdAt(LocalDateTime.now().minusDays(72)).build(),

            ConsultDoc.builder().id(UUID.randomUUID().toString()).consultId(1045L)
                .iamIssue("기기변경 공시지원금 vs 선택약정 비교 문의")
                .iamAction("공시지원금 선택약정 24개월 상세 비교 안내").content("고객이 공시지원금이랑 선약이랑 어떤 게 더 유리한지 물어봄. 본인 요금제 기준 비교 계산 안내.")
                .allText("공시지원금 선택약정 선약 비교 기기변경 24개월 할인").customerName("임공시").customerId("C045").sentiment("POSITIVE").riskScore(5).priority("LOW").phone("010-3007-0007").createdAt(LocalDateTime.now().minusDays(74)).build(),

            ConsultDoc.builder().id(UUID.randomUUID().toString()).consultId(1046L)
                .iamIssue("분실 핸드폰 분실 신고 및 임시 정지")
                .iamAction("분실 신고 후 회선 임시 정지 처리 완료").content("고객이 핸드폰을 잃어버렸다고 함. 분실 신고 접수 후 회선 임시 정지 및 위치 추적 안내.")
                .allText("분실신고 분실폰 임시정지 회선정지 위치추적 도난신고").customerName("나분실").customerId("C046").sentiment("NEGATIVE").riskScore(20).priority("NORMAL").phone("010-3008-0008").createdAt(LocalDateTime.now().minusDays(76)).build(),

            ConsultDoc.builder().id(UUID.randomUUID().toString()).consultId(1047L)
                .iamIssue("스마트워치 신규 개통 문의")
                .iamAction("갤럭시 워치 데이터 쉐어링 개통 처리").content("고객이 갤럭시 워치 새로 샀는데 독립 회선으로 개통하고 싶다고 함. 쉐어링 개통 처리.")
                .allText("스마트워치 갤럭시워치 독립회선 데이터쉐어링 개통").customerName("류워치").customerId("C047").sentiment("POSITIVE").riskScore(5).priority("LOW").phone("010-3009-0009").createdAt(LocalDateTime.now().minusDays(78)).build(),

            ConsultDoc.builder().id(UUID.randomUUID().toString()).consultId(1048L)
                .iamIssue("기기변경 취소 요청 - 개통 당일")
                .iamAction("개통 당일 청약 철회 처리 완료").content("고객이 어제 기변했는데 생각보다 마음에 안든다며 취소 원함. 청약철회 가능 기간 내 취소 처리.")
                .allText("기기변경취소 청약철회 개통취소 기변취소 반품 환불").customerName("문취소").customerId("C048").sentiment("NEUTRAL").riskScore(15).priority("NORMAL").phone("010-3010-0010").createdAt(LocalDateTime.now().minusDays(80)).build(),

            // ── 장애/AS ──────────────────────────────────────────────────────
            ConsultDoc.builder().id(UUID.randomUUID().toString()).consultId(1049L)
                .iamIssue("인터넷 연결 불가 - 갑자기 끊김")
                .iamAction("원격 점검 후 공유기 재부팅으로 해결").content("고객이 갑자기 인터넷이 안된다고 문의. 원격으로 공유기 라우터 재부팅 안내로 해결.")
                .allText("인터넷연결불가 인터넷끊김 공유기재부팅 라우터 원격점검 장애").customerName("조인터").customerId("C049").sentiment("POSITIVE").riskScore(20).priority("NORMAL").phone("010-4001-0001").createdAt(LocalDateTime.now().minusDays(82)).build(),

            ConsultDoc.builder().id(UUID.randomUUID().toString()).consultId(1050L)
                .iamIssue("IPTV U+tv 화면 검은 화면 장애")
                .iamAction("셋톱박스 리셋 및 HDMI 케이블 교체 안내").content("고객이 TV가 검은 화면만 나온다고 함. 셋탑 셋톱박스 리셋 및 HDMI 케이블 점검 안내.")
                .allText("IPTV tv장애 검은화면 셋톱박스 셋탑 hdmi 리셋 리모컨").customerName("박TV").customerId("C050").sentiment("NEGATIVE").riskScore(30).priority("HIGH").phone("010-4002-0002").createdAt(LocalDateTime.now().minusDays(84)).build(),

            ConsultDoc.builder().id(UUID.randomUUID().toString()).consultId(1051L)
                .iamIssue("모바일 데이터 연결 불가 - LTE 안됨")
                .iamAction("APN 설정 초기화 후 데이터 연결 정상화").content("고객이 데이터가 갑자기 안된다고 함. APN 설정 초기화 및 IMEI 확인 후 데이터 연결 복구.")
                .allText("데이터연결불가 lte 4g 데이터안됨 apn imei 초기화 장애").customerName("최데이터").customerId("C051").sentiment("NEGATIVE").riskScore(35).priority("HIGH").phone("010-4003-0003").createdAt(LocalDateTime.now().minusDays(86)).build(),

            ConsultDoc.builder().id(UUID.randomUUID().toString()).consultId(1052L)
                .iamIssue("와이파이 연결은 되는데 인터넷 속도 매우 느림")
                .iamAction("기가인터넷 속도 측정 후 기술팀 출동 예약").content("고객이 와이파이 wifi는 붙는데 속도가 너무 느리다고 함. 속도 측정 후 기술자 출동 예약.")
                .allText("와이파이 wifi 속도저하 인터넷느림 기가인터넷 속도측정 기술출동").customerName("오와이파이").customerId("C052").sentiment("NEGATIVE").riskScore(25).priority("NORMAL").phone("010-4004-0004").createdAt(LocalDateTime.now().minusDays(88)).build(),

            ConsultDoc.builder().id(UUID.randomUUID().toString()).consultId(1053L)
                .iamIssue("리모컨 오작동 - 버튼 안 눌림")
                .iamAction("리모컨 교체 신청 접수 및 무상 교체 처리").content("고객이 리모컨이 버튼이 잘 안눌린다고 함. 리모컨 리모콘 불량으로 무상 교체 처리.")
                .allText("리모컨 리모콘 오작동 버튼불량 교체 무상교체 셋탑박스").customerName("윤리모").customerId("C053").sentiment("POSITIVE").riskScore(5).priority("LOW").phone("010-4005-0005").createdAt(LocalDateTime.now().minusDays(90)).build(),

            ConsultDoc.builder().id(UUID.randomUUID().toString()).consultId(1054L)
                .iamIssue("5G 속도 불만 - 5G라는데 LTE보다 느림")
                .iamAction("5G 수신 지역 확인 및 5G 네트워크 설정 안내").content("고객이 5G 5세대 요금제인데 속도가 LTE lte보다 느리다고 불만. 5G 커버리지 확인 및 설정 안내.")
                .allText("5g 5세대 속도불만 lte 커버리지 네트워크 5g설정 통화품질").customerName("서속도").customerId("C054").sentiment("NEGATIVE").riskScore(40).priority("HIGH").phone("010-4006-0006").createdAt(LocalDateTime.now().minusDays(92)).build(),

            ConsultDoc.builder().id(UUID.randomUUID().toString()).consultId(1055L)
                .iamIssue("스마트홈 맘카 연결 오류")
                .iamAction("맘카 앱 재설치 및 wifi 재연결 안내").content("고객이 맘카 가정카메라 앱에서 기기 연결이 안된다고 함. 앱 재설치 및 와이파이 설정 안내.")
                .allText("스마트홈 맘카 홈카메라 가정카메라 연결오류 앱재설치 wifi").customerName("임스마트").customerId("C055").sentiment("NEGATIVE").riskScore(20).priority("NORMAL").phone("010-4007-0007").createdAt(LocalDateTime.now().minusDays(94)).build(),

            ConsultDoc.builder().id(UUID.randomUUID().toString()).consultId(1056L)
                .iamIssue("통화 중 음질 불량 - 상대방 소리 잘 안들림")
                .iamAction("통화 품질 개선 설정 안내 및 기지국 점검 요청").content("고객이 통화할 때 상대방 소리가 끊기고 잘 안들린다고 함. 통화품질불량 기지국 점검 신청.")
                .allText("통화품질불량 통화끊김 음질불량 기지국 통화음질 장애신고").customerName("나통화").customerId("C056").sentiment("NEGATIVE").riskScore(30).priority("HIGH").phone("010-4008-0008").createdAt(LocalDateTime.now().minusDays(96)).build(),

            ConsultDoc.builder().id(UUID.randomUUID().toString()).consultId(1057L)
                .iamIssue("인터넷전화 홈전화 불통 장애")
                .iamAction("홈전화 회선 점검 후 정상화 처리").content("고객이 홈전화 인터넷전화가 안된다고 함. 유선전화 회선 점검 후 장애 조치 완료.")
                .allText("홈전화 인터넷전화 유선전화 불통 장애 회선점검").customerName("류홈폰").customerId("C057").sentiment("POSITIVE").riskScore(15).priority("NORMAL").phone("010-4009-0009").createdAt(LocalDateTime.now().minusDays(98)).build(),

            ConsultDoc.builder().id(UUID.randomUUID().toString()).consultId(1058L)
                .iamIssue("기기 발열 심함 - 배터리 빨리 닳음")
                .iamAction("배터리 점검 및 AS 센터 방문 안내").content("고객이 최근 핸드폰이 발열이 심하고 배터리가 너무 빨리 닳는다고 함. AS 센터 방문 안내.")
                .allText("발열 배터리 배터리교체 충전불량 기기AS 발열문제 배터리불량").customerName("문발열").customerId("C058").sentiment("NEGATIVE").riskScore(15).priority("NORMAL").phone("010-4010-0010").createdAt(LocalDateTime.now().minusDays(100)).build(),

            // ── 부가서비스 ───────────────────────────────────────────────────
            ConsultDoc.builder().id(UUID.randomUUID().toString()).consultId(1059L)
                .iamIssue("넷플릭스 부가서비스 가입 문의")
                .iamAction("넷플릭스 요금제 연동 방법 안내 및 신청").content("고객이 넷플릭스 netflix 같이 쓸 수 있는 요금제 있냐고 문의. OTT 결합 상품 안내.")
                .allText("넷플릭스 netflix 넷플 OTT 부가서비스 결합상품 영상").customerName("조넷플").customerId("C059").sentiment("POSITIVE").riskScore(3).priority("LOW").phone("010-5001-0001").createdAt(LocalDateTime.now().minusDays(102)).build(),

            ConsultDoc.builder().id(UUID.randomUUID().toString()).consultId(1060L)
                .iamIssue("U+폰케어 보험 가입 문의 - 파손 보장 범위")
                .iamAction("U+폰케어 프리미엄 파손 분실 보장 내용 안내").content("고객이 폰케어 유플러스폰케어 가입하고 싶은데 파손 분실 얼마까지 되냐고 문의. 상세 안내.")
                .allText("U+폰케어 폰케어프리미엄 휴대폰보험 파손보장 분실보장 스마트폰보험").customerName("박폰케어").customerId("C060").sentiment("POSITIVE").riskScore(5).priority("LOW").phone("010-5002-0002").createdAt(LocalDateTime.now().minusDays(104)).build(),

            ConsultDoc.builder().id(UUID.randomUUID().toString()).consultId(1061L)
                .iamIssue("아이들나라 부가서비스 해지 문의")
                .iamAction("아이들나라 해지 처리 완료").content("고객이 아이들나라 아이들네이션 더 이상 안 쓴다며 해지 원함. 해지 처리 완료 안내.")
                .allText("아이들나라 아이들네이션 부가서비스해지 해지").customerName("최아이들").customerId("C061").sentiment("NEUTRAL").riskScore(5).priority("LOW").phone("010-5003-0003").createdAt(LocalDateTime.now().minusDays(106)).build(),

            ConsultDoc.builder().id(UUID.randomUUID().toString()).consultId(1062L)
                .iamIssue("로밍패스 미국 출장 데이터 문의")
                .iamAction("미국 로밍패스 5GB 30일 신청 처리").content("고객이 미국 출장 2주 예정이라 로밍패스 문의. 5GB 44000원 30일 상품 안내 및 신청.")
                .allText("로밍패스 미국로밍 해외로밍 데이터로밍 출장 로밍 미국").customerName("오로밍").customerId("C062").sentiment("POSITIVE").riskScore(5).priority("LOW").phone("010-5004-0004").createdAt(LocalDateTime.now().minusDays(108)).build(),

            ConsultDoc.builder().id(UUID.randomUUID().toString()).consultId(1063L)
                .iamIssue("유튜브 프리미엄 결합 요금제 문의")
                .iamAction("유튜브 프리미엄 포함 요금제 안내 및 변경").content("고객이 유튜브프리미엄 유튜브유료 포함된 요금제로 바꾸고 싶다고 함. 안내 및 변경 처리.")
                .allText("유튜브프리미엄 유튜브유료 결합할인 요금제 부가서비스").customerName("윤유튜브").customerId("C063").sentiment("POSITIVE").riskScore(5).priority("LOW").phone("010-5005-0005").createdAt(LocalDateTime.now().minusDays(110)).build(),

            ConsultDoc.builder().id(UUID.randomUUID().toString()).consultId(1064L)
                .iamIssue("멤버십 포인트 사용 방법 문의")
                .iamAction("U+ 멤버십 포인트 사용처 및 혜택 안내").content("고객이 U+멤버십 유플러스멤버십 포인트 쌓여있는데 어떻게 쓰냐고 문의. 사용처 안내.")
                .allText("U+멤버십 멤버십포인트 포인트적립 포인트사용 VIP혜택").customerName("서멤버십").customerId("C064").sentiment("POSITIVE").riskScore(3).priority("LOW").phone("010-5006-0006").createdAt(LocalDateTime.now().minusDays(112)).build(),

            ConsultDoc.builder().id(UUID.randomUUID().toString()).consultId(1065L)
                .iamIssue("데이터 1GB 추가 구매 문의")
                .iamAction("데이터 1GB 추가 구매 1100원 신청 처리").content("고객이 이번 달 데이터가 다 떨어졌다고 추가로 살 수 있냐고 함. 데이터 1GB 추가 처리.")
                .allText("데이터추가 데이터부족 데이터구매 데이터1gb 데이터충전").customerName("임데이터").customerId("C065").sentiment("POSITIVE").riskScore(3).priority("LOW").phone("010-5007-0007").createdAt(LocalDateTime.now().minusDays(114)).build(),

            ConsultDoc.builder().id(UUID.randomUUID().toString()).consultId(1066L)
                .iamIssue("스팸 전화 차단 서비스 가입 문의")
                .iamAction("스팸전화 알림 서비스 무료 신청 처리").content("고객이 스팸전화 보이스피싱 의심 전화가 너무 많이 온다고 차단 서비스 문의. 무료 신청 완료.")
                .allText("스팸전화알림 스팸차단 스팸전화 보이스피싱 피싱차단 안심서비스").customerName("나스팸").customerId("C066").sentiment("POSITIVE").riskScore(10).priority("LOW").phone("010-5008-0008").createdAt(LocalDateTime.now().minusDays(116)).build(),

            ConsultDoc.builder().id(UUID.randomUUID().toString()).consultId(1067L)
                .iamIssue("디즈니플러스 티빙 결합 문의")
                .iamAction("디즈니플러스 티빙 결합 요금제 안내").content("고객이 디플 디즈니플러스하고 티빙 tving 둘 다 보고 싶다고 결합 문의. 요금제 안내.")
                .allText("디즈니플러스 디플 티빙 tving OTT 결합 부가서비스 영상서비스").customerName("류디즈니").customerId("C067").sentiment("POSITIVE").riskScore(3).priority("LOW").phone("010-5009-0009").createdAt(LocalDateTime.now().minusDays(118)).build(),

            ConsultDoc.builder().id(UUID.randomUUID().toString()).consultId(1068L)
                .iamIssue("뮤직벨링 서비스 해지 문의")
                .iamAction("뮤직벨링 해지 처리 및 일할 계산 환불").content("고객이 뮤직벨링 통화연결음 서비스 안쓰는데 계속 빠져나간다며 해지 원함. 해지 처리.")
                .allText("뮤직벨링 통화연결음 부가서비스해지 일할계산 환불").customerName("문뮤직").customerId("C068").sentiment("NEUTRAL").riskScore(5).priority("LOW").phone("010-5010-0010").createdAt(LocalDateTime.now().minusDays(120)).build(),

            // ── 위험/민원 고위험 ─────────────────────────────────────────────
            ConsultDoc.builder().id(UUID.randomUUID().toString()).consultId(1069L)
                .iamIssue("소비자원 민원 신고 예고 - 서비스 불만")
                .iamAction("소비자원 신고 예방을 위한 적극 해결 처리").content("고객이 소비자원에 신고하겠다고 협박성 발언. 방통위에 민원도 넣겠다고 함. 적극 해결 처리.")
                .allText("소비자원 방통위 민원 신고예고 협박 외부기관위협 언론신고").customerName("조소비자원").customerId("C069").sentiment("NEGATIVE").riskScore(88).priority("URGENT").phone("010-6001-0001").createdAt(LocalDateTime.now().minusDays(122)).build(),

            ConsultDoc.builder().id(UUID.randomUUID().toString()).consultId(1070L)
                .iamIssue("욕설 및 반복 전화 고질 민원 고객")
                .iamAction("감정노동 보호 절차 진행 - 3차 경고 후 종료").content("고객이 반복민원 고질민원 이력 있음. 이번에도 욕설 블랙컨슈머 행위 지속. 상습민원 처리.")
                .allText("욕설 반복민원 고질민원 블랙컨슈머 상습민원 감정노동 보호").customerName("박반복").customerId("C070").sentiment("NEGATIVE").riskScore(95).priority("URGENT").phone("010-6002-0002").createdAt(LocalDateTime.now().minusDays(124)).build(),

            ConsultDoc.builder().id(UUID.randomUUID().toString()).consultId(1071L)
                .iamIssue("스미싱 문자 피해 신고 - 개인정보 유출 의심")
                .iamAction("스미싱 피해 신고 접수 및 보안 조치 강화").content("고객이 유플러스에서 온 것처럼 위장한 스미싱 문자를 받았다고 피해 신고. 피싱해킹 대응 처리.")
                .allText("스미싱 보이스피싱 피싱피해 피싱해킹 개인정보유출 사기 부정사용").customerName("최스미싱").customerId("C071").sentiment("NEGATIVE").riskScore(90).priority("URGENT").phone("010-6003-0003").createdAt(LocalDateTime.now().minusDays(126)).build(),

            ConsultDoc.builder().id(UUID.randomUUID().toString()).consultId(1072L)
                .iamIssue("법적 조치 예고 - 위약금 분쟁")
                .iamAction("위약금 분쟁 예방을 위한 법무팀 연결 처리").content("고객이 위약금 때문에 소송 법적조치 하겠다고 함. 소송예고 민원예고 상황. 법무팀 연결.")
                .allText("법적조치 소송예고 위약금분쟁 소비자원 민원예고 해지위약금").customerName("오법무").customerId("C072").sentiment("NEGATIVE").riskScore(87).priority("URGENT").phone("010-6004-0004").createdAt(LocalDateTime.now().minusDays(128)).build(),

            ConsultDoc.builder().id(UUID.randomUUID().toString()).consultId(1073L)
                .iamIssue("명의도용 부정 개통 신고")
                .iamAction("명의도용 부정개통 신고 접수 및 회선 즉시 정지").content("고객이 자기 명의로 모르는 번호가 개통됐다고 신고. 부정사용 명의도용 즉시 조치.")
                .allText("명의도용 부정개통 부정사용 사기의심 명의변경 신고").customerName("윤명의").customerId("C073").sentiment("NEGATIVE").riskScore(92).priority("URGENT").phone("010-6005-0005").createdAt(LocalDateTime.now().minusDays(130)).build(),

            ConsultDoc.builder().id(UUID.randomUUID().toString()).consultId(1074L)
                .iamIssue("과도한 위약금 면제 요구 - 정책 악용 의심")
                .iamAction("위약금 정책 설명 후 예외 적용 불가 안내").content("고객이 이런저런 이유로 위약금 면제 계속 요구. 정책악용 혜택악용 의심. 면제 불가 안내.")
                .allText("위약금면제 정책악용 혜택악용 제도악용 과도한요구 꼼수").customerName("서정책").customerId("C074").sentiment("NEGATIVE").riskScore(70).priority("HIGH").phone("010-6006-0006").createdAt(LocalDateTime.now().minusDays(132)).build(),

            ConsultDoc.builder().id(UUID.randomUUID().toString()).consultId(1075L)
                .iamIssue("해지 위협 반복 - 이탈위험 고객 대응")
                .iamAction("특별 할인 혜택 제공으로 해지 방어 성공").content("고객이 3개월째 해지하겠다고 반복 연락. 이탈위험 해지위험 고객. 특별 혜택 제시로 방어.")
                .allText("해지위협 이탈위험 해지위험 해지예정 해지방어 반복전화 잔류").customerName("임해지위").customerId("C075").sentiment("NEGATIVE").riskScore(83).priority("URGENT").phone("010-6007-0007").createdAt(LocalDateTime.now().minusDays(134)).build(),

            ConsultDoc.builder().id(UUID.randomUUID().toString()).consultId(1076L)
                .iamIssue("불완전 판매 주장 - 요금제 설명 부족 주장")
                .iamAction("판매 당시 상담 이력 확인 후 사실 여부 설명").content("고객이 요금제 가입 시 충분한 설명을 못 들었다며 불완전판매 주장. 이의제기 접수.")
                .allText("불완전판매 요금이의 이의제기 설명부족 소비자원 민원").customerName("나불완전").customerId("C076").sentiment("NEGATIVE").riskScore(75).priority("HIGH").phone("010-6008-0008").createdAt(LocalDateTime.now().minusDays(136)).build(),

            ConsultDoc.builder().id(UUID.randomUUID().toString()).consultId(1077L)
                .iamIssue("언론 신고 예고 - SNS 후기 작성 협박")
                .iamAction("서비스 불만 적극 해결로 SNS 게시 철회 유도").content("고객이 SNS에 나쁜 후기 올리겠다고 언론신고 협박. 외부기관위협 상황. 적극 해결 처리.")
                .allText("SNS후기 언론신고 협박 외부기관위협 민원예고 사과응대").customerName("류협박").customerId("C077").sentiment("NEGATIVE").riskScore(80).priority("URGENT").phone("010-6009-0009").createdAt(LocalDateTime.now().minusDays(138)).build(),

            ConsultDoc.builder().id(UUID.randomUUID().toString()).consultId(1078L)
                .iamIssue("보상 요구 - 장애로 인한 손해배상 주장")
                .iamAction("장애 보상 정책 안내 및 합리적 보상 처리").content("고객이 인터넷 장애로 재택근무 못했다며 손해배상 환불요구. 연속장애 보상 정책 안내.")
                .allText("손해배상 환불요구 보상요구 과도한보상 연속장애 장애보상 배상요구").customerName("문보상").customerId("C078").sentiment("NEGATIVE").riskScore(73).priority("HIGH").phone("010-6010-0010").createdAt(LocalDateTime.now().minusDays(140)).build(),

            // ── 기타/일반 문의 ───────────────────────────────────────────────
            ConsultDoc.builder().id(UUID.randomUUID().toString()).consultId(1079L)
                .iamIssue("매장 위치 및 영업시간 문의")
                .iamAction("가까운 대리점 위치 및 영업시간 안내").content("고객이 집 근처 유플러스 대리점 위치 알고 싶다고 문의. 가까운 직영점 안내.")
                .allText("매장위치 대리점 직영점 영업시간 오프라인 방문").customerName("조매장").customerId("C079").sentiment("POSITIVE").riskScore(3).priority("LOW").phone("010-7001-0001").createdAt(LocalDateTime.now().minusDays(142)).build(),

            ConsultDoc.builder().id(UUID.randomUUID().toString()).consultId(1080L)
                .iamIssue("명의 변경 - 부모님 명의로 변경 요청")
                .iamAction("명의변경 서류 안내 및 절차 설명").content("고객이 현재 본인 명의를 부모님 명의로 변경하고 싶다고 함. 명의변경 소유권이전 서류 안내.")
                .allText("명의변경 명의이전 소유권이전 부모명의 서류 신분증").customerName("박명의").customerId("C080").sentiment("NEUTRAL").riskScore(5).priority("LOW").phone("010-7002-0002").createdAt(LocalDateTime.now().minusDays(144)).build(),

            ConsultDoc.builder().id(UUID.randomUUID().toString()).consultId(1081L)
                .iamIssue("청구서 이메일 수신 변경 문의")
                .iamAction("이메일 청구서 수신 주소 변경 처리").content("고객이 청구서 고지서 받는 이메일 주소 변경하고 싶다고 함. 주소 변경 완료.")
                .allText("청구서 고지서 이메일변경 주소변경 전자고지 페이퍼리스").customerName("최이메일").customerId("C081").sentiment("POSITIVE").riskScore(3).priority("LOW").phone("010-7003-0003").createdAt(LocalDateTime.now().minusDays(146)).build(),

            ConsultDoc.builder().id(UUID.randomUUID().toString()).consultId(1082L)
                .iamIssue("영수증 발급 문의 - 세금계산서 요청")
                .iamAction("법인 세금계산서 발급 절차 안내").content("고객이 회사에서 법인 세금계산서 필요하다고 발급 요청. 법인고객 기업고객 세금계산서 안내.")
                .allText("세금계산서 영수증발급 법인고객 기업고객 B2B 법인").customerName("오세금").customerId("C082").sentiment("NEUTRAL").riskScore(5).priority("LOW").phone("010-7004-0004").createdAt(LocalDateTime.now().minusDays(148)).build(),

            ConsultDoc.builder().id(UUID.randomUUID().toString()).consultId(1083L)
                .iamIssue("군인 요금제 가입 문의 - 현역병사")
                .iamAction("현역병사 요금제 가입 처리 완료").content("고객이 군입대 예정이라 군인 요금제 현역병사 플랜 문의. 가입 처리 완료.")
                .allText("현역병사 군인요금제 병사요금제 군인플랜 입대 군입대").customerName("윤군인").customerId("C083").sentiment("POSITIVE").riskScore(3).priority("LOW").phone("010-7005-0005").createdAt(LocalDateTime.now().minusDays(150)).build(),

            ConsultDoc.builder().id(UUID.randomUUID().toString()).consultId(1084L)
                .iamIssue("청소년 요금제 문의 - 중학생 자녀")
                .iamAction("5G 청소년 요금제 가입 처리").content("고객이 중학생 자녀 위해 청소년 요금제 물어봄. 5G 청소년 학생요금제 33000원 안내 및 가입.")
                .allText("청소년요금제 5g청소년 학생요금제 중고생요금제 자녀 중학생").customerName("서청소").customerId("C084").sentiment("POSITIVE").riskScore(3).priority("LOW").phone("010-7006-0006").createdAt(LocalDateTime.now().minusDays(152)).build(),

            ConsultDoc.builder().id(UUID.randomUUID().toString()).consultId(1085L)
                .iamIssue("키즈폰 요금제 문의 - 초등학생 자녀")
                .iamAction("5G 키즈 요금제 가입 및 자녀 보호 서비스 안내").content("고객이 초등학생 아이한테 폰 처음 사줬는데 키즈 요금제 물어봄. 5G 키즈 33000원 안내.")
                .allText("키즈요금제 5g키즈 어린이요금제 아이요금제 초등학생 자녀 키즈").customerName("임키즈").customerId("C085").sentiment("POSITIVE").riskScore(3).priority("LOW").phone("010-7007-0007").createdAt(LocalDateTime.now().minusDays(154)).build(),

            ConsultDoc.builder().id(UUID.randomUUID().toString()).consultId(1086L)
                .iamIssue("사은품 경품 미수령 문의")
                .iamAction("사은품 발송 현황 확인 및 재발송 처리").content("고객이 기변 신청할 때 사은품 준다고 해서 기다리는데 아직도 안왔다고 문의. 재발송 처리.")
                .allText("사은품 경품 사은품미수령 재발송 기기변경 혜택").customerName("나사은품").customerId("C086").sentiment("NEGATIVE").riskScore(20).priority("NORMAL").phone("010-7008-0008").createdAt(LocalDateTime.now().minusDays(156)).build(),

            ConsultDoc.builder().id(UUID.randomUUID().toString()).consultId(1087L)
                .iamIssue("주소 변경 - 이사 후 청구지 주소 변경")
                .iamAction("청구지 주소 변경 처리 완료").content("고객이 이사해서 청구서 받을 주소 바꿔달라고 함. 주소변경 완료 처리.")
                .allText("주소변경 이사 청구지변경 고객정보변경 배송지").customerName("류주소").customerId("C087").sentiment("POSITIVE").riskScore(3).priority("LOW").phone("010-7009-0009").createdAt(LocalDateTime.now().minusDays(158)).build(),

            ConsultDoc.builder().id(UUID.randomUUID().toString()).consultId(1088L)
                .iamIssue("서비스 개선 칭찬 및 제안")
                .iamAction("고객 제안 내용 접수 및 감사 인사 전달").content("고객이 최근 상담 서비스가 너무 친절했다고 칭찬 전화. 서비스 개선 제안도 함께 전달.")
                .allText("칭찬 서비스개선제안 친절응대 고객만족 긍정감정 감사인사").customerName("문칭찬").customerId("C088").sentiment("POSITIVE").riskScore(0).priority("LOW").phone("010-7010-0010").createdAt(LocalDateTime.now().minusDays(160)).build(),

            // ── 혼합 시나리오 ────────────────────────────────────────────────
            ConsultDoc.builder().id(UUID.randomUUID().toString()).consultId(1089L)
                .iamIssue("VVIP 고객 해지위협 - 요금 불만")
                .iamAction("VVIP 특별 요금 감면 및 혜택 추가 적용").content("VVIP 브이브이아이피 다이아몬드 우수고객이 요금이 비싸다며 해지 협박. 특별 혜택 제공.")
                .allText("vvip 브이브이아이피 다이아몬드 해지위협 이탈위험 요금감면 혜택").customerName("조VVIP").customerId("C089").sentiment("NEGATIVE").riskScore(82).priority("URGENT").phone("010-8001-0001").createdAt(LocalDateTime.now().minusDays(162)).build(),

            ConsultDoc.builder().id(UUID.randomUUID().toString()).consultId(1090L)
                .iamIssue("갤럭시 기기변경 후 와이파이 연결 불가 장애")
                .iamAction("갤럭시 wifi 설정 초기화 안내로 해결").content("고객이 갤폰 갤럭시 새로 기변했는데 와이파이 wifi 연결이 안된다고 함. 설정 초기화로 해결.")
                .allText("갤럭시 갤폰 기기변경 와이파이 wifi 연결불가 설정초기화 장애").customerName("박와이파이").customerId("C090").sentiment("POSITIVE").riskScore(15).priority("NORMAL").phone("010-8002-0002").createdAt(LocalDateTime.now().minusDays(164)).build(),

            ConsultDoc.builder().id(UUID.randomUUID().toString()).consultId(1091L)
                .iamIssue("번이 후 넷플릭스 결합 유지 문의")
                .iamAction("번이 후 OTT 결합 서비스 유지 가능 안내").content("고객이 타사로 번이 고려 중인데 넷플릭스 결합은 유지되냐고 문의. 해지 방어 후 설명.")
                .allText("번이 번호이동 넷플릭스 결합 OTT 해지방어 타사이동").customerName("최번이넷").customerId("C091").sentiment("NEGATIVE").riskScore(78).priority("HIGH").phone("010-8003-0003").createdAt(LocalDateTime.now().minusDays(166)).build(),

            ConsultDoc.builder().id(UUID.randomUUID().toString()).consultId(1092L)
                .iamIssue("미납 후 부정사용 의심 - 요금 폭탄")
                .iamAction("부정사용 신고 접수 및 요금 조정 처리").content("고객이 자기가 쓰지 않은 로밍 데이터로밍 요금이 엄청 나왔다고 부정사용 신고. 요금 조정.")
                .allText("부정사용 요금폭탄 로밍 데이터로밍 미납 요금이의 부정개통").customerName("오부정사").customerId("C092").sentiment("NEGATIVE").riskScore(85).priority("URGENT").phone("010-8004-0004").createdAt(LocalDateTime.now().minusDays(168)).build(),

            ConsultDoc.builder().id(UUID.randomUUID().toString()).consultId(1093L)
                .iamIssue("노인 시니어 요금제 할인 문의")
                .iamAction("65세 이상 시니어 감면 요금제 안내 및 변경").content("고객이 65세 이상 노인이라 싸게 쓸 수 있는 요금제 있냐고 문의. 시니어가족결합 안내.")
                .allText("시니어 노인요금제 65세이상 시니어가족결합 고령자 경로").customerName("윤시니어").customerId("C093").sentiment("POSITIVE").riskScore(5).priority("LOW").phone("010-8005-0005").createdAt(LocalDateTime.now().minusDays(170)).build(),

            ConsultDoc.builder().id(UUID.randomUUID().toString()).consultId(1094L)
                .iamIssue("법인 다회선 요금제 일괄 변경 문의")
                .iamAction("법인 기업 고객 다회선 요금제 일괄 변경 처리").content("고객이 법인고객 기업고객으로 직원 20명 요금제 일괄 변경 원함. B2B 담당자 연결 처리.")
                .allText("법인고객 기업고객 다회선 일괄변경 B2B 기업요금제").customerName("서법인").customerId("C094").sentiment("POSITIVE").riskScore(5).priority("NORMAL").phone("010-8006-0006").createdAt(LocalDateTime.now().minusDays(172)).build(),

            ConsultDoc.builder().id(UUID.randomUUID().toString()).consultId(1095L)
                .iamIssue("너겟 요금제 변경 문의 - 데이터 용량 증가")
                .iamAction("너겟 7GB에서 너겟 무제한으로 변경 처리").content("고객이 너겟 nugget 유스 유쓰 요금제 쓰는데 데이터 자꾸 부족하다고 업그레이드 원함.")
                .allText("너겟 nugget 유쓰 youth 유스 데이터부족 요금제변경 업그레이드").customerName("임너겟").customerId("C095").sentiment("POSITIVE").riskScore(5).priority("LOW").phone("010-8007-0007").createdAt(LocalDateTime.now().minusDays(174)).build(),

            ConsultDoc.builder().id(UUID.randomUUID().toString()).consultId(1096L)
                .iamIssue("헬로비전 인터넷에서 U+ 인터넷으로 변경")
                .iamAction("헬로비전 해지 후 U+ 인터넷 신규 가입 처리").content("고객이 LG헬로비전 인터넷에서 유플러스 인터넷으로 바꾸고 싶다고 함. 신규 설치 예약.")
                .allText("헬로비전 LG헬로비전 유플러스인터넷 인터넷변경 신규설치").customerName("나헬로").customerId("C096").sentiment("POSITIVE").riskScore(5).priority("LOW").phone("010-8008-0008").createdAt(LocalDateTime.now().minusDays(176)).build(),

            ConsultDoc.builder().id(UUID.randomUUID().toString()).consultId(1097L)
                .iamIssue("이중납부 중복 결제 환불 요청")
                .iamAction("이중납부 확인 후 즉시 환불 처리").content("고객이 이번 달 요금이 두 번 빠져나갔다고 문의. 이중납부 이중청구 확인 후 즉시 환불.")
                .allText("이중납부 중복결제 이중청구 환불 청구오류 납부금액").customerName("류이중납").customerId("C097").sentiment("NEGATIVE").riskScore(45).priority("HIGH").phone("010-8009-0009").createdAt(LocalDateTime.now().minusDays(178)).build(),

            ConsultDoc.builder().id(UUID.randomUUID().toString()).consultId(1098L)
                .iamIssue("분할 납부 신청 - VIP 고객 요금 연체")
                .iamAction("VIP 고객 특별 분납 12개월 처리").content("고객이 VIP vip 우수고객인데 이번 달 요금이 많이 나와서 분납 나눠내기 신청. 12개월 처리.")
                .allText("vip 브이아이피 우수고객 분납 분할납부 나눠내기 요금연체").customerName("문VIP").customerId("C098").sentiment("NEUTRAL").riskScore(30).priority("NORMAL").phone("010-8010-0010").createdAt(LocalDateTime.now().minusDays(180)).build(),

            ConsultDoc.builder().id(UUID.randomUUID().toString()).consultId(1099L)
                .iamIssue("아이폰 eSIM 설정 - 갤럭시에서 전환")
                .iamAction("아이폰 esim 설정 원격 안내 완료").content("고객이 갤폰에서 아이폰으로 기변했는데 esim 유심 설정 어떻게 하냐고 문의. 원격 안내.")
                .allText("아이폰 iphone esim 유심 갤폰 기기변경 나노유심 물리유심").customerName("조아폰eSIM").customerId("C099").sentiment("POSITIVE").riskScore(5).priority("LOW").phone("010-9001-0001").createdAt(LocalDateTime.now().minusDays(182)).build(),

            ConsultDoc.builder().id(UUID.randomUUID().toString()).consultId(1100L)
                .iamIssue("해지 후 재가입 혜택 및 사은품 문의")
                .iamAction("재가입 혜택 안내 및 사은품 신청 처리").content("고객이 다른 통신사 갔다가 다시 유플러스로 돌아오고 싶다고 재가입 혜택 사은품 문의.")
                .allText("재가입 혜택 사은품 경품 유플러스 복귀 번호이동").customerName("박재가입").customerId("C100").sentiment("POSITIVE").riskScore(5).priority("LOW").phone("010-9002-0002").createdAt(LocalDateTime.now().minusDays(184)).build()
        );

        // ES 저장
        docs.forEach(consultSearchService::saveConsultation);

        // MongoDB 동시 저장 — ES ↔ MongoDB consultId 연결
        // consultation_summary 컬렉션에 매핑 데이터 적재
        docs.forEach(doc -> {
            // 기존 문서가 있으면 삭제 후 재적재 (멱등성 보장)
            summaryRepository.findByConsultId(doc.getConsultId())
                    .ifPresent(existing -> summaryRepository.delete(existing));

            ConsultationSummary summary = ConsultationSummary.builder()
                    .consultId(doc.getConsultId())
                    .consultedAt(doc.getCreatedAt())
                    .channel("CALL")
                    .durationSec(300)
                    .agent(ConsultationSummary.Agent.builder()
                            ._id(1L)
                            .name("테스트 상담사")
                            .build())
                    .category(ConsultationSummary.Category.builder()
                            .code("M_ETC_01")
                            .large("기타")
                            .medium("일반 문의")
                            .small("테스트 데이터")
                            .build())
                    .iam(ConsultationSummary.Iam.builder()
                            .issue(doc.getIamIssue())
                            .action(doc.getIamAction())
                            .memo(doc.getIamMemo())
                            .matchRates(0.85)
                            .build())
                    .summary(ConsultationSummary.Summary.builder()
                            .status("COMPLETED")
                            .content(doc.getContent())
                            .keywords(doc.getAllText() != null
                                    ? java.util.Arrays.asList(doc.getAllText().split(" ")).stream()
                                            .filter(k -> k.length() >= 2)
                                            .limit(5)
                                            .toList()
                                    : java.util.List.of())
                            .build())
                    .riskFlags(doc.getRiskScore() != null && doc.getRiskScore() >= 80
                            ? java.util.List.of("해지위험")
                            : doc.getRiskScore() != null && doc.getRiskScore() >= 50
                                    ? java.util.List.of("반복민원")
                                    : java.util.List.of())
                    .customer(ConsultationSummary.Customer.builder()
                            ._id(doc.getConsultId())
                            .name(doc.getCustomerName())
                            .phone(doc.getPhone())
                            .type("개인")
                            .grade(doc.getRiskScore() != null && doc.getRiskScore() >= 80 ? "VIP" : "일반")
                            .satisfiedScore(doc.getSentiment() != null && doc.getSentiment().equals("POSITIVE") ? 4.5
                                    : doc.getSentiment() != null && doc.getSentiment().equals("NEGATIVE") ? 2.0
                                    : 3.5)
                            .build())
                    .cancellation(ConsultationSummary.Cancellation.builder()
                            .intent(doc.getIamIssue() != null && doc.getIamIssue().contains("해지"))
                            .defenseAttempted(doc.getIamAction() != null && doc.getIamAction().contains("해지방어"))
                            .defenseSuccess(false)
                            .build())
                    .createdAt(doc.getCreatedAt())
                    .build();

            summaryRepository.save(summary);
        });

        return ResponseEntity.ok("테스트 데이터 " + docs.size() + "건 ES + MongoDB 동시 저장 완료!" +
                " (consultId: 1001~1018)");
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
