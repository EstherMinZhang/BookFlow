package com.bookflow.orders;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

// 结账请求：指定顾客以及要购买的图书列表。
public class CheckoutRequest {

    @NotNull
    private Long customerId;

    @Valid
    @NotEmpty
    private List<CheckoutItemRequest> items = new ArrayList<>();

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public List<CheckoutItemRequest> getItems() {
        return items;
    }

    public void setItems(List<CheckoutItemRequest> items) {
        this.items = items;
    }
}
