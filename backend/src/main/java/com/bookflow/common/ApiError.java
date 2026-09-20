package com.bookflow.common;

import java.time.Instant;
import java.util.List;

// 统一错误响应结构，让前端能用一致的格式展示错误信息。
public class ApiError {

    // 错误发生时间，便于调试和日志排查。
    private final Instant timestamp;
    private final int status;
    private final String error;
    private final String message;
    // details 用于存放字段校验错误等更具体的信息。
    private final List<String> details;

    public ApiError(int status, String error, String message, List<String> details) {
        this.timestamp = Instant.now();
        this.status = status;
        this.error = error;
        this.message = message;
        this.details = details;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public List<String> getDetails() {
        return details;
    }
}
