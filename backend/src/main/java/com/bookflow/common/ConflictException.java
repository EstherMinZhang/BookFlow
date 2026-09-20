package com.bookflow.common;

// 业务冲突异常，例如 ISBN 重复时返回 409 Conflict。
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
