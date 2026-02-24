package com.uplus.crm.domain.account.service;

import com.uplus.crm.domain.account.dto.response.EmailCheckResponseDto;
import com.uplus.crm.domain.account.repository.mysql.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final EmployeeRepository employeeRepository;

    /**
     * 구글 이메일 중복 확인
     * @param email 확인할 이메일
     * @return 사용 가능 여부와 이메일을 담은 DTO
     */
    public EmailCheckResponseDto checkEmailAvailability(String email) {
        // DB에 해당 이메일이 이미 존재하면 available은 false가 됩니다.
        boolean isDuplicate = employeeRepository.existsByEmail(email);
        
        return EmailCheckResponseDto.builder()
                .available(!isDuplicate)
                .email(email)
                .build();
    }
}