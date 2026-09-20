package com.bookflow.orders;

import java.math.BigDecimal;

// 返回给前端的订单项 DTO。
public class OrderItemResponse {

    private Long bookId;
    private String title;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;

    public OrderItemResponse(OrderItem item) {
        bookId = item.getBook().getId();
        title = item.getBook().getTitle();
        quantity = item.getQuantity();
        unitPrice = item.getUnitPrice();
        lineTotal = item.getLineTotal();
    }

    public Long getBookId() {
        return bookId;
    }

    public String getTitle() {
        return title;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }
}
