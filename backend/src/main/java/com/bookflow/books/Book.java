package com.bookflow.books;

import java.math.BigDecimal;
import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

@Entity
@Table(name = "books")
public class Book {

    // 数据库主键，使用数据库自增策略生成。
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 图书标题、作者和 ISBN 是库存管理中最基础的图书信息。
    @Column(nullable = false, length = 160)
    private String title;

    @Column(nullable = false, length = 120)
    private String author;

    // ISBN 要求唯一，避免系统中出现重复图书记录。
    @Column(nullable = false, unique = true, length = 32)
    private String isbn;

    // 使用 BigDecimal 保存金额，避免 double/float 带来的精度问题。
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    // 当前可售库存数量，后续结账时会在同一个事务中扣减。
    @Column(nullable = false)
    private Integer stockQuantity;

    // 审计字段：记录创建时间和最近更新时间。
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected Book() {
    }

    // 业务构造函数：用于创建新的图书库存记录。
    public Book(String title, String author, String isbn, BigDecimal price, Integer stockQuantity) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.price = price;
        this.stockQuantity = stockQuantity;
    }

    @PrePersist
    void onCreate() {
        // 首次保存前自动写入创建时间和更新时间。
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        // 每次更新实体前刷新 updatedAt，方便前端展示最近变更。
        updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
