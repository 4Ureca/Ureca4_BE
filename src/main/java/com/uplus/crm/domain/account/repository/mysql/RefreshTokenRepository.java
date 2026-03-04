package com.uplus.crm.domain.account.repository.mysql;

import com.uplus.crm.domain.account.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Integer> {

    Optional<RefreshToken> findByRefreshToken(String refreshToken);

    @Transactional
    void deleteByEmployee_EmpId(Integer empId);

    @Transactional
    void deleteByRefreshToken(String refreshToken);
}