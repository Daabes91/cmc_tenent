package com.clinic.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility for validating API response format consistency.
 * Ensures all responses follow standardized structure and formatting rules.
 */
public class ResponseFormatValidator {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Validation result for response format checking.
     */
    public static class ValidationResult {
        private final boolean valid;
        private final List<String> errors;

        public ValidationResult(boolean valid, List<String> errors) {
            this.valid = valid;
            this.errors = errors != null ? errors : new ArrayList<>();
        }

        public boolean isValid() { return valid; }
        public List<String> getErrors() { return errors; }

        @Override
        public String toString() {
            return valid ? "Valid" : "Invalid: " + String.join(", ", errors);
        }
    }

    /**
     * Validate API response format.
     */
    public static ValidationResult validateResponse(Object response) {
        List<String> errors = new ArrayList<>();

        try {
            JsonNode jsonNode = objectMapper.valueToTree(response);
            
            // Check for required fields in success response
            if (jsonNode.has("success")) {
                boolean success = jsonNode.get("success").asBoolean();
                
                if (success) {
                    validateSuccessResponse(jsonNode, errors);
                } else {
                    validateErrorResponse(jsonNode, errors);
                }
            } else {
                errors.add("Missing required 'success' field");
            }

            // Check timestamp format
            validateTimestamp(jsonNode, errors);

        } catch (Exception e) {
            errors.add("Failed to parse response as JSON: " + e.getMessage());
        }

        return new ValidationResult(errors.isEmpty(), errors);
    }

    /**
     * Validate paginated response format.
     */
    public static ValidationResult validatePaginatedResponse(Object response) {
        List<String> errors = new ArrayList<>();

        try {
            JsonNode jsonNode = objectMapper.valueToTree(response);
            
            // Check basic response structure
            ValidationResult basicValidation = validateResponse(response);
            if (!basicValidation.isValid()) {
                return basicValidation;
            }

            // Check pagination structure
            if (jsonNode.has("data")) {
                JsonNode dataNode = jsonNode.get("data");
                
                if (dataNode.has("pagination")) {
                    validatePaginationMetadata(dataNode.get("pagination"), errors);
                } else {
                    errors.add("Missing 'pagination' field in data object");
                }

                if (!dataNode.has("data")) {
                    errors.add("Missing 'data' array in data object");
                }
            }

        } catch (Exception e) {
            errors.add("Failed to validate paginated response: " + e.getMessage());
        }

        return new ValidationResult(errors.isEmpty(), errors);
    }

    /**
     * Validate currency format in response.
     */
    public static ValidationResult validateCurrencyFormat(JsonNode currencyNode) {
        List<String> errors = new ArrayList<>();

        if (currencyNode != null && currencyNode.isObject()) {
            if (!currencyNode.has("amount")) {
                errors.add("Currency object missing 'amount' field");
            }
            if (!currencyNode.has("currency")) {
                errors.add("Currency object missing 'currency' field");
            }
            if (!currencyNode.has("formatted")) {
                errors.add("Currency object missing 'formatted' field");
            }
        } else if (currencyNode != null) {
            errors.add("Currency should be an object with amount, currency, and formatted fields");
        }

        return new ValidationResult(errors.isEmpty(), errors);
    }

    private static void validateSuccessResponse(JsonNode jsonNode, List<String> errors) {
        if (!jsonNode.has("data")) {
            errors.add("Success response missing 'data' field");
        }
        
        if (jsonNode.has("error_code")) {
            errors.add("Success response should not have 'error_code' field");
        }
        
        if (jsonNode.has("errors")) {
            errors.add("Success response should not have 'errors' field");
        }
    }

    private static void validateErrorResponse(JsonNode jsonNode, List<String> errors) {
        if (!jsonNode.has("error_code")) {
            errors.add("Error response missing 'error_code' field");
        }
        
        if (!jsonNode.has("message")) {
            errors.add("Error response missing 'message' field");
        }
        
        if (jsonNode.has("data") && !jsonNode.get("data").isNull()) {
            errors.add("Error response should not have non-null 'data' field");
        }
    }

    private static void validateTimestamp(JsonNode jsonNode, List<String> errors) {
        if (jsonNode.has("timestamp")) {
            String timestamp = jsonNode.get("timestamp").asText();
            if (!TimestampFormatter.isValidIso8601(timestamp)) {
                errors.add("Invalid timestamp format, should be ISO 8601: " + timestamp);
            }
        } else {
            errors.add("Missing required 'timestamp' field");
        }
    }

    private static void validatePaginationMetadata(JsonNode paginationNode, List<String> errors) {
        String[] requiredFields = {"page", "page_size", "total", "total_pages", 
                                 "has_next", "has_previous", "first", "last"};
        
        for (String field : requiredFields) {
            if (!paginationNode.has(field)) {
                errors.add("Pagination metadata missing '" + field + "' field");
            }
        }
    }
}