package com.clinic.api;

import java.util.List;
import java.util.Map;

public record ApiResponse<T>(
        boolean success,
        String code,
        String message,
        T data,
        Map<String, Object> meta,
        Map<String, String> links,
        List<ApiError> errors
) {
}
