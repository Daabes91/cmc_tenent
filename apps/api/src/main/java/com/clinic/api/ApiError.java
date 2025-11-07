package com.clinic.api;

public record ApiError(String field, String message) {

    public static ApiError of(String field, String message) {
        return new ApiError(field, message);
    }
}
