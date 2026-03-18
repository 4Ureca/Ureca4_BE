package com.uplus.crm.domain.extraction.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uplus.crm.domain.extraction.dto.response.FailedAnalysisDto;
import com.uplus.crm.domain.extraction.repository.ResultEventStatusRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalysisAdminService {

    private final ResultEventStatusRepository resultRepository;

    public List<FailedAnalysisDto> getIntegratedFailedList() {
        return resultRepository.findIntegratedFailedTasks();
    }
}