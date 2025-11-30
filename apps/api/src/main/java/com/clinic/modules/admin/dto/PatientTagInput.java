package com.clinic.modules.admin.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Flexible tag input that accepts either an ID (number) or an object with id/name.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PatientTagInput(Long id, String name) {

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static PatientTagInput from(Object node) {
        if (node instanceof Number number) {
            return new PatientTagInput(number.longValue(), null);
        }

        if (node instanceof JsonNode json) {
            Long parsedId = json.hasNonNull("id") ? json.get("id").asLong() : null;
            String parsedName = json.hasNonNull("name") ? json.get("name").asText() : null;
            return new PatientTagInput(parsedId, parsedName);
        }

        // Fallback: attempt to parse stringified numbers as IDs, otherwise treat as name
        if (node instanceof String str) {
            try {
                return new PatientTagInput(Long.parseLong(str), null);
            } catch (NumberFormatException ex) {
                return new PatientTagInput(null, str);
            }
        }

        return new PatientTagInput(null, null);
    }
}
