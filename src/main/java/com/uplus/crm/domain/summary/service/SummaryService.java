package com.uplus.crm.domain.summary.service;

import com.uplus.crm.common.exception.BusinessException;
import com.uplus.crm.common.exception.ErrorCode;
import com.uplus.crm.domain.common.repository.ProductAdditionalRepository;
import com.uplus.crm.domain.common.repository.ProductHomeRepository;
import com.uplus.crm.domain.common.repository.ProductMobileRepository;
import com.uplus.crm.domain.summary.document.ConsultationSummary;
import com.uplus.crm.domain.summary.dto.response.ConsultationSummaryDetailResponse;
import com.uplus.crm.domain.summary.repository.SummaryConsultationResultRepository;
import com.uplus.crm.domain.summary.repository.SummaryRepository;
import java.util.*;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SummaryService {

  private final SummaryRepository summaryRepository;
  private final SummaryConsultationResultRepository consultationResultRepository;

  private final ProductMobileRepository productMobileRepository;
  private final ProductHomeRepository productHomeRepository;
  private final ProductAdditionalRepository productAdditionalRepository;

  public ConsultationSummaryDetailResponse getDetail(Long id) {

    if (!consultationResultRepository.existsById(id)) {
      throw new BusinessException(ErrorCode.CONSULTATION_RESULT_NOT_FOUND);
    }

    ConsultationSummary entity =
        summaryRepository.findByConsultId(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.SUMMARY_NOT_FOUND));

    Map<String, String> productNameMap = buildProductNameMap(entity);

    return ConsultationSummaryDetailResponse.from(entity, productNameMap);
  }

  private Map<String, String> buildProductNameMap(ConsultationSummary entity) {

    if (entity.getResultProducts() == null) {
      return Map.of();
    }

    List<String> productCodes =
        entity.getResultProducts().stream()
            .flatMap(p -> {

              Stream<String> subscribed =
                  p.getSubscribed() == null ? Stream.empty() : p.getSubscribed().stream();

              Stream<String> canceled =
                  p.getCanceled() == null ? Stream.empty() : p.getCanceled().stream();

              Stream<String> recommitment =
                  p.getRecommitment() == null ? Stream.empty() : p.getRecommitment().stream();

              Stream<String> conversion =
                  p.getConversion() == null
                      ? Stream.empty()
                      : p.getConversion().stream()
                          .flatMap(c -> Stream.of(c.getSubscribed(), c.getCanceled()));

              return Stream.of(subscribed, canceled, recommitment, conversion)
                  .flatMap(s -> s);
            })
            .filter(Objects::nonNull)
            .distinct()
            .toList();

    Map<String, String> productNameMap = new HashMap<>();

    productMobileRepository.findByMobileCodeIn(productCodes)
        .forEach(p -> productNameMap.put(p.getMobileCode(), p.getPlanName()));

    productHomeRepository.findByHomeCodeIn(productCodes)
        .forEach(p -> productNameMap.put(p.getHomeCode(), p.getProductName()));

    productAdditionalRepository.findByAdditionalCodeIn(productCodes)
        .forEach(p -> productNameMap.put(p.getAdditionalCode(), p.getAdditionalName()));

    return productNameMap;
  }
}