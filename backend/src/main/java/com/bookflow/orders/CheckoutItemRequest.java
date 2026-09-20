package com.bookflow.orders;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

// 结账请求中的单个图书条目。
public class CheckoutItemRequest {

    @NotNull
    private Long bookId;

    @NotNull
    @Min(1)
    private Integer quantity;

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
