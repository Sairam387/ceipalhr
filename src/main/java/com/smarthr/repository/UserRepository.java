package com.smarthr.repository;

import com.smarthr.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsernameIgnoreCase(String username);

    boolean existsByUsernameIgnoreCase(String username);

    List<User> findByRoleIgnoreCase(String role);

    List<User> findByActiveTrue();

    List<User> findByEmployeeIdIgnoreCase(String employeeId);
}