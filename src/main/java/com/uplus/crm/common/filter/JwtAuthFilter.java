package com.uplus.crm.common.filter;

import com.uplus.crm.common.security.CustomUserDetails;
import com.uplus.crm.common.util.JwtUtil;
import com.uplus.crm.domain.account.entity.Employee;
import com.uplus.crm.domain.account.repository.mysql.EmployeeRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * JWT 검증 후 SecurityContext에 CustomUserDetails(empId + roleName)를 등록.
 * EmployeeRepository.findByIdWithDetails()로 DB에서 역할을 조회한다.
 *
 * ⚠️ 요청마다 DB 조회가 발생한다.
 *    성능 개선이 필요하다면 JWT 클레임에 role을 포함하거나 Redis 캐시를 도입하라.
 * ⚠️ @Component 없이 SecurityConfig에서 직접 생성 — Servlet 필터 체인 이중 등록 방지.
 */
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final EmployeeRepository employeeRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            if (jwtUtil.isValid(token)) {
                Integer empId = jwtUtil.getEmpId(token);

                String roleName    = resolveRoleName(empId);
                String grantedRole = "관리자".equals(roleName) ? "ROLE_ADMIN" : "ROLE_AGENT";

                CustomUserDetails userDetails = new CustomUserDetails(empId, roleName);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                List.of(new SimpleGrantedAuthority(grantedRole))
                        );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }

    private String resolveRoleName(Integer empId) {
        return employeeRepository.findByIdWithDetails(empId)
                .map(Employee::getEmployeeDetail)
                .map(detail -> detail.getJobRole().getRoleName())
                .orElse("상담사"); // fallback
    }
}
