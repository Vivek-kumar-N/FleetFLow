
 package com.carcaddy.repository;

import com.carcaddy.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EmployeeRespository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByEmailId(String emailId);

    List<Employee> findByDesignation(String designation);

    List<Employee> findByAccountActive(Boolean accountActive);

    List<Employee> findByExpiryDateBeforeAndAccountActive(
            LocalDate date,
            Boolean accountActive
    );
}



