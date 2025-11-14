package com.clinic.modules.core.translation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@Component
public class DefaultTranslationProvider {

    private static final Logger log = LoggerFactory.getLogger(DefaultTranslationProvider.class);

    private final Map<String, Map<String, String>> localeTranslations = new HashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public DefaultTranslationProvider() {
        loadLocale("en", "i18n/messages-en.json");
        loadLocale("ar", "i18n/messages-ar.json");
    }

    private void loadLocale(String locale, String resourcePath) {
        try (InputStream inputStream = new ClassPathResource(resourcePath).getInputStream()) {
            Map<String, Object> raw = objectMapper.readValue(inputStream, new TypeReference<Map<String, Object>>() {});
            Map<String, String> flattened = new LinkedHashMap<>();
            flatten("", raw, flattened);
            localeTranslations.put(locale.toLowerCase(Locale.ROOT), flattened);
            log.info("Loaded {} default translations for locale {}", flattened.size(), locale);
        } catch (IOException ex) {
            log.warn("Failed to load default translations for locale {} from {}: {}", locale, resourcePath, ex.getMessage());
            localeTranslations.put(locale.toLowerCase(Locale.ROOT), Map.of());
        }
    }

    @SuppressWarnings("unchecked")
    private void flatten(String prefix, Map<String, Object> source, Map<String, String> target) {
        source.forEach((key, value) -> {
            String path = prefix.isEmpty() ? key : prefix + "." + key;
            if (value instanceof Map<?, ?> mapValue) {
                flatten(path, (Map<String, Object>) mapValue, target);
            } else if (value instanceof String stringValue) {
                target.put(path, stringValue);
            }
        });
    }

    public Map<String, String> getDefaults(String locale) {
        if (locale == null) {
            return localeTranslations.getOrDefault("en", Map.of());
        }
        return localeTranslations.getOrDefault(locale.toLowerCase(Locale.ROOT), localeTranslations.getOrDefault("en", Map.of()));
    }
}
