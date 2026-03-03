package com.uplus.crm.domain.consultation.service;

import com.uplus.crm.common.exception.BusinessException;
import com.uplus.crm.common.exception.ErrorCode;
import com.uplus.crm.domain.consultation.dto.response.ConsultDataResponse;
import com.uplus.crm.domain.consultation.dto.response.SubscribedProduct;
import com.uplus.crm.domain.consultation.entity.ConsultationCategoryPolicy;
import com.uplus.crm.domain.consultation.entity.ConsultationResult;
import com.uplus.crm.domain.consultation.entity.Customer;
import com.uplus.crm.domain.consultation.repository.ConsultationCategoryRepository;
import com.uplus.crm.domain.consultation.repository.ConsultationResultRepository;
import com.uplus.crm.domain.consultation.repository.CustomerRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ConsultationService {

    private final ConsultationResultRepository consultationResultRepository;
    private final CustomerRepository customerRepository;
    private final ConsultationCategoryRepository categoryRepository;

    /**
     * DB에서 랜덤 상담결과 1건 조회 → 고객정보 + 상담기본정보 + IAM 필드 포함 반환.
     */
    @Transactional(readOnly = true)
    public ConsultDataResponse getRandomConsultData() {
        ConsultationResult result = consultationResultRepository.findOneRandom()
                .orElseThrow(() -> new BusinessException(ErrorCode.CONSULTATION_NOT_FOUND));

        Customer customer = customerRepository.findById(result.getCustomerId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CONSULTATION_NOT_FOUND));

        ConsultationCategoryPolicy category = categoryRepository.findById(result.getCategoryCode())
                .orElseThrow(() -> new BusinessException(ErrorCode.CONSULTATION_NOT_FOUND));

        List<SubscribedProduct> subscribedProducts = customerRepository
                .findActiveSubscribedProducts(customer.getCustomerId())
                .stream()
                .map(p -> new SubscribedProduct(
                        p.getProductType(), p.getProductCode(), p.getProductName(), p.getCategory()))
                .toList();

        return new ConsultDataResponse(
                result.getConsultId(),
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
                result.getIamIssue(),
                result.getIamAction(),
                result.getIamMemo()
        );
    }
}
