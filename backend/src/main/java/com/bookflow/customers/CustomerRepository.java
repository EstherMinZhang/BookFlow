package com.bookflow.customers;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

// 顾客数据访问层，负责顾客 CRUD 和邮箱唯一性查询。
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    boolean existsByEmail(String email);

    Optional<Customer> findByEmail(String email);
}
