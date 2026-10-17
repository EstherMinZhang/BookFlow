package com.bookflow;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class CheckoutItemRequest {
    @NotNull public Long bookId;
    @NotNull @Min(1) public Integer quantity;
}