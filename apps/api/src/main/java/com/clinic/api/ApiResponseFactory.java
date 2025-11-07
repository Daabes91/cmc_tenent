package com.clinic.api;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class ApiResponseFactory {

    private ApiResponseFactory() {
    }

    public static <T> ApiResponse<T> success(String code, String message, T data) {
        return success(code, message, data, Collections.emptyMap(), Collections.emptyMap());
    }

    public static <T> ApiResponse<T> success(String code,
                                             String message,
                                             T data,
                                             Map<String, Object> additionalMeta,
                                             Map<String, String> links) {
        return new ApiResponse<>(
                true,
                code,
                message,
                data,
                buildMeta(additionalMeta),
                links == null || links.isEmpty() ? null : Map.copyOf(links),
                null
        );
    }

    public static ApiResponse<Void> success(String code, String message) {
        return success(code, message, null);
    }

    public static ApiResponse<Void> success(String code,
                                            String message,
                                            Map<String, Object> additionalMeta,
                                            Map<String, String> links) {
        return success(code, message, null, additionalMeta, links);
    }

    public static ApiResponse<Void> error(String code,
                                          String message,
                                          List<ApiError> errors) {
        return error(code, message, errors, Collections.emptyMap());
    }

    public static ApiResponse<Void> error(String code,
                                          String message,
                                          List<ApiError> errors,
                                          Map<String, Object> additionalMeta) {
        List<ApiError> errorList = (errors == null || errors.isEmpty()) ? List.of() : List.copyOf(errors);
        return new ApiResponse<>(
                false,
                code,
                message,
                null,
                buildMeta(additionalMeta),
                null,
                errorList
        );
    }

    @SuppressWarnings("unchecked")
    public static <T> ApiResponse<T> errorWithType(String code,
                                                   String message,
                                                   List<ApiError> errors) {
        return (ApiResponse<T>) error(code, message, errors);
    }

    private static Map<String, Object> buildMeta(Map<String, Object> additionalMeta) {
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("timestamp", Instant.now().toString());
        meta.put("requestId", UUID.randomUUID().toString());
        if (additionalMeta != null && !additionalMeta.isEmpty()) {
            meta.putAll(additionalMeta);
        }
        return meta;
    }
}
