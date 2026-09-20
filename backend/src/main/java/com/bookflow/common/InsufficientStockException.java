package com.bookflow.common;

// 库存不足异常，结账时请求数量超过可售库存会抛出。
public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(String message) {
        super(message);
    }
}
