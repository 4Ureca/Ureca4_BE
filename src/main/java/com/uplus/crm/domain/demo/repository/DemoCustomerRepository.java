package com.uplus.crm.domain.demo.repository;

import com.uplus.crm.domain.demo.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DemoCustomerRepository extends JpaRepository<Customer, Long> {
}
