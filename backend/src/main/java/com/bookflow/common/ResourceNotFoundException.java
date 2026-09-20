package com.bookflow.common;

// 资源不存在异常，例如访问不存在的图书 id 时返回 404 Not Found。
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
