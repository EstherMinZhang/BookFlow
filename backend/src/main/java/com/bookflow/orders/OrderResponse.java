package com.bookflow.orders;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

// 返回给前端的订单 DTO。
public class OrderResponse {

    private Long id;
    private Long customerId;
    private String customerName;
    private BigDecimal totalAmount;
    private Instant createdAt;
    private List<OrderItemResponse> items;

    public OrderResponse(CustomerOrder order) {
        id = order.getId();
        customerId = order.getCustomer().getId();
        customerName = order.getCustomer().getName();
        totalAmount = order.getTotalAmount();
        createdAt = order.getCreatedAt();
        items = order.getItems().stream()
                .map(OrderItemResponse::new)
                .collect(Collectors.toList());
    }

    public Long getId() {
        return id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public List<OrderItemResponse> getItems() {
        return items;
    }
}
