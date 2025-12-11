package com.clinic.modules.saas.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Serializes LocalDateTime to epoch seconds (UTC).
 */
public class LocalDateTimeToEpochMillisSerializer extends JsonSerializer<LocalDateTime> {
    @Override
    public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }
        long epochSeconds = value.toInstant(ZoneOffset.UTC).getEpochSecond();
        gen.writeNumber(epochSeconds);
    }
}
