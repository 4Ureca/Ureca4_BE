package com.uplus.crm.domain.extraction.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uplus.crm.domain.extraction.dto.response.WeeklyExcellentCaseResponse;
import com.uplus.crm.domain.extraction.repository.WeeklyExcellentCaseRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WeeklyExcellentCaseService {
    private final WeeklyExcellentCaseRepository weeklyRepository;

    public List<WeeklyExcellentCaseResponse> getWeeklyBoard(Integer year, Integer week) {
        return weeklyRepository.findWeeklyCases(year, week);
    }
}