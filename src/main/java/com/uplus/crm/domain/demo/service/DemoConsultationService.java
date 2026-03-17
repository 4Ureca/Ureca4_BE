package com.uplus.crm.domain.demo.service;

import com.uplus.crm.common.exception.BusinessException;
import com.uplus.crm.common.exception.ErrorCode;
import com.uplus.crm.domain.demo.dto.request.DemoConsultSubmitRequest;
import com.uplus.crm.domain.demo.dto.response.DemoConsultDataResponse;
import com.uplus.crm.domain.demo.dto.response.DemoSubscribedProduct;
import com.uplus.crm.domain.demo.dto.response.DemoConsultSubmitResponse;
import com.uplus.crm.domain.consultation.entity.ConsultationCategoryPolicy;
import com.uplus.crm.domain.consultation.entity.ConsultationRawText;
import com.uplus.crm.domain.consultation.entity.ConsultationResult;
import com.uplus.crm.domain.consultation.entity.Customer;
import com.uplus.crm.domain.consultation.repository.ConsultationRawTextRepository;
import com.uplus.crm.domain.extraction.entity.ExcellentEventStatus;
import com.uplus.crm.domain.extraction.entity.ResultEventStatus;
import com.uplus.crm.domain.extraction.entity.SummaryEventStatus;
import com.uplus.crm.domain.extraction.repository.ExcellentEventStatusRepository;
import com.uplus.crm.domain.extraction.repository.ResultEventStatusRepository;
import com.uplus.crm.domain.extraction.repository.SummaryEventStatusRepository;
import org.springframework.util.StringUtils;
import com.uplus.crm.domain.demo.repository.DemoConsultationCategoryRepository;
import com.uplus.crm.domain.demo.repository.DemoConsultationResultRepository;
import com.uplus.crm.domain.demo.repository.DemoCustomerRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DemoConsultationService {

    private final DemoConsultationResultRepository consultationResultRepository;
    private final DemoCustomerRepository customerRepository;
    private final DemoConsultationCategoryRepository categoryRepository;
    private final ConsultationRawTextRepository rawTextRepository;
    private final ResultEventStatusRepository resultEventStatusRepository;
    private final ExcellentEventStatusRepository excellentEventStatusRepository;
    private final SummaryEventStatusRepository summaryEventStatusRepository;

    /**
     * DB에서 랜덤 상담결과 1건 조회 → 고객정보 + 상담기본정보 반환 (IAM 필드는 null).
     */
    @Transactional(readOnly = true)
    public DemoConsultDataResponse getRandomConsultData() {
        ConsultationResult result = consultationResultRepository.findOneRandom()
                .orElseThrow(() -> new BusinessException(ErrorCode.CONSULTATION_NOT_FOUND));

        Customer customer = customerRepository.findById(result.getCustomerId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CONSULTATION_NOT_FOUND));

        ConsultationCategoryPolicy category = categoryRepository.findById(result.getCategoryCode())
                .orElseThrow(() -> new BusinessException(ErrorCode.CONSULTATION_NOT_FOUND));

        List<DemoSubscribedProduct> subscribedProducts = customerRepository
                .findActiveSubscribedProducts(customer.getCustomerId())
                .stream()
                .map(p -> new DemoSubscribedProduct(
                        p.getProductType(), p.getProductCode(), p.getProductName(), p.getCategory()))
                .toList();

        String rawTextJson = rawTextRepository.findFirstByConsultId(result.getConsultId())
                .map(ConsultationRawText::getRawTextJson)
                .orElse(null);

        return new DemoConsultDataResponse(
                customer.getCustomerId(),
                customer.getName(),
                customer.getPhone(),
                customer.getCustomerType(),
                customer.getGender(),
                customer.getBirthDate(),
                customer.getGradeCode(),
                customer.getEmail(),
                subscribedProducts,
                result.getChannel(),
                category.getCategoryCode(),
                category.getLargeCategory(),
                category.getMediumCategory(),
                category.getSmallCategory(),
                result.getDurationSec(),
                null,
                null,
                null,
                rawTextJson
        );
    }

    /**
     * 프론트에서 IAM 3필드 작성 후 전송 → 신규 상담결과 row 삽입.
     */
    @Transactional
    public DemoConsultSubmitResponse submitConsult(DemoConsultSubmitRequest request, int empId) {
        ConsultationResult saved = consultationResultRepository.save(
                ConsultationResult.builder()
                        .empId(empId)
                        .customerId(request.customerId())
                        .channel(request.channel())
                        .categoryCode(request.categoryCode())
                        .durationSec(request.durationSec())
                        .iamIssue(request.iamIssue())
                        .iamAction(request.iamAction())
                        .iamMemo(request.iamMemo())
                        .build()
        );

        if (StringUtils.hasText(request.rawTextJson())) {
            rawTextRepository.save(
                    ConsultationRawText.builder()
                            .consultId(saved.getConsultId())
                            .rawTextJson(request.rawTextJson())
                            .build()
            );
        }

        resultEventStatusRepository.save(
                ResultEventStatus.builder()
                        .consultId(saved.getConsultId())
                        .categoryCode(request.categoryCode())
                        .build()
        );

        excellentEventStatusRepository.save(
                ExcellentEventStatus.builder()
                        .consultId(saved.getConsultId())
                        .build()
        );

        summaryEventStatusRepository.save(
                SummaryEventStatus.builder()
                        .consultId(saved.getConsultId())
                        .build()
        );

        return new DemoConsultSubmitResponse(saved.getConsultId(), saved.getCreatedAt());
    }
}
