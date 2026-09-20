package com.bookflow.books;

import java.math.BigDecimal;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

// 前端创建或更新图书时提交的请求体 DTO。
public class BookRequest {

    // 标题不能为空，并限制最大长度，避免无效或过长的数据进入数据库。
    @NotBlank
    @Size(max = 160)
    private String title;

    // 作者不能为空。
    @NotBlank
    @Size(max = 120)
    private String author;

    // ISBN 不能为空；唯一性由 Service 层结合数据库查询校验。
    @NotBlank
    @Size(max = 32)
    private String isbn;

    // 价格必须存在且不能为负数。
    @NotNull
    @DecimalMin(value = "0.00")
    private BigDecimal price;

    // 库存必须存在且不能为负数。
    @NotNull
    @Min(0)
    private Integer stockQuantity;

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
}
