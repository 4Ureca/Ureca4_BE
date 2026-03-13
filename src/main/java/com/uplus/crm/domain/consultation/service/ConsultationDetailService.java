package com.uplus.crm.domain.consultation.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uplus.crm.common.exception.BusinessException;
import com.uplus.crm.common.exception.ErrorCode;
import com.uplus.crm.domain.consultation.dto.response.ConsultationAiAnalysisDto;
import com.uplus.crm.domain.consultation.dto.response.ConsultationBasicInfoDto;
import com.uplus.crm.domain.consultation.dto.response.ConsultationDetailResponseDto;
import com.uplus.crm.domain.consultation.dto.response.ConsultationHistoryItemDto;
import com.uplus.crm.domain.consultation.dto.response.ConsultationIamInfoDto;
import com.uplus.crm.domain.consultation.entity.ConsultationRawText;
import com.uplus.crm.domain.consultation.repository.ConsultationDetailQueryRepository;
import com.uplus.crm.domain.consultation.repository.ConsultationRawTextRepository;
import com.uplus.crm.domain.consultation.repository.CustomerRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConsultationDetailService {

    private final ConsultationDetailQueryRepository consultationDetailQueryRepository;
    private final ConsultationRawTextRepository consultationRawTextRepository;
    private final CustomerRepository customerRepository;

    public ConsultationDetailResponseDto getConsultationDetail(Long consultId) {
        ConsultationDetailQueryRepository.BasicInfoRow basicRow =
                consultationDetailQueryRepository.findBasicInfo(consultId);

        if (basicRow == null) {
            throw new BusinessException(ErrorCode.CONSULTATION_NOT_FOUND);
        }

        String relatedProducts = customerRepository.findActiveSubscribedProducts(basicRow.customerId())
                .stream()
                .map(SubscribedProductProjection -> SubscribedProductProjection.getProductName())
                .collect(Collectors.joining(", "));

        String tags = basicRow.gradeCode() == null ? "" : basicRow.gradeCode();

        ConsultationBasicInfoDto basicInfo = ConsultationBasicInfoDto.builder()
                .consultId(basicRow.consultId())
                .consultationNumber(basicRow.consultationNumber())
                .customerName(basicRow.customerName())
                .customerPhone(basicRow.customerPhone())
                .customerEmail(basicRow.customerEmail())
                .channel(basicRow.channel())
                .consultationType(basicRow.consultationType())
                .processStatus(basicRow.processStatus())
                .counselorName(basicRow.counselorName())
                .satisfaction(basicRow.satisfaction())
                .consultedAt(basicRow.consultedAt())
                .durationMinutes(basicRow.durationMinutes() == null ? 0 : basicRow.durationMinutes() / 60)
                .relatedProducts(relatedProducts)
                .tags(tags)
                .build();

        ConsultationDetailQueryRepository.AiAnalysisRow aiRow =
                consultationDetailQueryRepository.findAiAnalysis(consultId);

        ConsultationIamInfoDto iamInfo = ConsultationIamInfoDto.builder()
                .title(buildIamTitle(basicRow.mediumCategory(), basicRow.smallCategory()))
                .content(buildIamContent(basicRow.iamIssue(), basicRow.iamAction()))
                .aiSummary(aiRow == null || aiRow.rawSummary() == null ? "-" : aiRow.rawSummary())
                .memo(basicRow.iamMemo() == null ? "" : basicRow.iamMemo())
                .build();

        String rawTextJson = consultationRawTextRepository.findFirstByConsultId(consultId)
                .map(ConsultationRawText::getRawTextJson)
                .orElse(null);

        ConsultationAiAnalysisDto aiAnalysis = ConsultationAiAnalysisDto.builder()
                .categoryCode(basicRow.categoryCode())
                .categoryName(buildCategoryName(basicRow.largeCategory(), basicRow.mediumCategory(), basicRow.smallCategory()))
                .hasIntent(aiRow == null ? null : aiRow.hasIntent())
                .complaintReason(aiRow == null ? null : aiRow.complaintReason())
                .defenseAttempted(aiRow == null ? null : aiRow.defenseAttempted())
                .defenseSuccess(aiRow == null ? null : aiRow.defenseSuccess())
                .defenseActions(aiRow == null ? null : aiRow.defenseActions())
                .rawSummary(aiRow == null ? null : aiRow.rawSummary())
                .build();

        List<ConsultationHistoryItemDto> history = consultationDetailQueryRepository.findHistory(consultId)
                .stream()
                .map(this::toHistoryItem)
                .toList();

        return ConsultationDetailResponseDto.builder()
                .basicInfo(basicInfo)
                .iamInfo(iamInfo)
                .rawTextJson(rawTextJson)
                .aiAnalysis(aiAnalysis)
                .history(history)
                .build();
    }

    private String buildIamTitle(String mediumCategory, String smallCategory) {
        if (smallCategory != null && !smallCategory.isBlank()) {
            return smallCategory + " 문의";
        }
        if (mediumCategory != null && !mediumCategory.isBlank()) {
            return mediumCategory + " 문의";
        }
        return "상담 문의";
    }

    private String buildIamContent(String iamIssue, String iamAction) {
        String issue = iamIssue == null ? "" : iamIssue;
        String action = iamAction == null ? "" : iamAction;

        if (issue.isBlank() && action.isBlank()) {
            return "";
        }
        if (action.isBlank()) {
            return issue;
        }
        if (issue.isBlank()) {
            return action;
        }
        return issue + "\n\n" + action;
    }

    private String buildCategoryName(String largeCategory, String mediumCategory, String smallCategory) {
        StringBuilder sb = new StringBuilder();

        if (largeCategory != null && !largeCategory.isBlank()) {
            sb.append(largeCategory);
        }
        if (mediumCategory != null && !mediumCategory.isBlank()) {
            if (!sb.isEmpty()) sb.append(" > ");
            sb.append(mediumCategory);
        }
        if (smallCategory != null && !smallCategory.isBlank()) {
            if (!sb.isEmpty()) sb.append(" > ");
            sb.append(smallCategory);
        }

        return sb.toString();
    }

    private ConsultationHistoryItemDto toHistoryItem(ConsultationDetailQueryRepository.HistoryRow row) {
        String title = switch (row.contractType()) {
            case "NEW" -> "신규 가입";
            case "CANCEL" -> "해지";
            case "CHANGE" -> "변경";
            case "RENEW" -> "재약정";
            default -> "변경 이력";
        };

        StringBuilder desc = new StringBuilder();
        if (row.productType() != null) {
            desc.append("상품유형: ").append(row.productType());
        }
        if (row.newProductHome() != null) {
            if (!desc.isEmpty()) desc.append(" / ");
            desc.append("신규 홈: ").append(row.newProductHome());
        }
        if (row.newProductMobile() != null) {
            if (!desc.isEmpty()) desc.append(" / ");
            desc.append("신규 모바일: ").append(row.newProductMobile());
        }
        if (row.newProductService() != null) {
            if (!desc.isEmpty()) desc.append(" / ");
            desc.append("신규 부가서비스: ").append(row.newProductService());
        }
        if (row.canceledProductHome() != null) {
            if (!desc.isEmpty()) desc.append(" / ");
            desc.append("해지 홈: ").append(row.canceledProductHome());
        }
        if (row.canceledProductMobile() != null) {
            if (!desc.isEmpty()) desc.append(" / ");
            desc.append("해지 모바일: ").append(row.canceledProductMobile());
        }
        if (row.canceledProductService() != null) {
            if (!desc.isEmpty()) desc.append(" / ");
            desc.append("해지 부가서비스: ").append(row.canceledProductService());
        }

        return ConsultationHistoryItemDto.builder()
                .occurredAt(row.occurredAt())
                .title(title)
                .description(desc.toString())
                .build();
    }
}
