package com.bookflow.common;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 处理资源不存在，将业务异常转换为标准 404 JSON 响应。
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException exception) {
        return build(HttpStatus.NOT_FOUND, exception.getMessage(), null);
    }

    // 处理业务冲突，例如重复 ISBN。
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiError> handleConflict(ConflictException exception) {
        return build(HttpStatus.CONFLICT, exception.getMessage(), null);
    }

    // 处理库存不足，返回 409 Conflict 表示当前库存状态无法满足请求。
    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ApiError> handleInsufficientStock(InsufficientStockException exception) {
        return build(HttpStatus.CONFLICT, exception.getMessage(), null);
    }

    // 处理 @Valid 校验失败，并把所有字段错误整理到 details 中。
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException exception) {
        List<String> details = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .collect(Collectors.toList());
        return build(HttpStatus.BAD_REQUEST, "Request validation failed.", details);
    }

    // 兜底处理未预期异常，避免把堆栈细节直接暴露给前端。
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception exception) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error.", null);
    }

    // 生成统一的错误响应对象。
    private ResponseEntity<ApiError> build(HttpStatus status, String message, List<String> details) {
        ApiError error = new ApiError(status.value(), status.getReasonPhrase(), message, details);
        return ResponseEntity.status(status).body(error);
    }
}
