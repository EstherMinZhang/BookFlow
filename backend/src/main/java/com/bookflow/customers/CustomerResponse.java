package com.bookflow.customers;

import java.time.Instant;

// 返回给前端的顾客响应 DTO。
public class CustomerResponse {

    private Long id;
    private String name;
    private String email;
    private String phone;
    private Instant createdAt;
    private Instant updatedAt;

    public CustomerResponse(Customer customer) {
        id = customer.getId();
        name = customer.getName();
        email = customer.getEmail();
        phone = customer.getPhone();
        createdAt = customer.getCreatedAt();
        updatedAt = customer.getUpdatedAt();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
