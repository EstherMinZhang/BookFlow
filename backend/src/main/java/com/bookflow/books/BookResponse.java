package com.bookflow.books;

import java.math.BigDecimal;
import java.time.Instant;

// 返回给前端的图书响应 DTO，避免直接暴露 JPA 实体。
public class BookResponse {

    private Long id;
    private String title;
    private String author;
    private String isbn;
    private BigDecimal price;
    private Integer stockQuantity;
    private Instant createdAt;
    private Instant updatedAt;

    // 从实体转换成 API 响应对象，Controller 只返回前端需要的数据。
    public BookResponse(Book book) {
        id = book.getId();
        title = book.getTitle();
        author = book.getAuthor();
        isbn = book.getIsbn();
        price = book.getPrice();
        stockQuantity = book.getStockQuantity();
        createdAt = book.getCreatedAt();
        updatedAt = book.getUpdatedAt();
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getIsbn() {
        return isbn;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
