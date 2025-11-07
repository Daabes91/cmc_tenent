package com.clinic.security;

import org.springframework.util.ResourceUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

/**
 * Utility helper for loading PEM encoded keys that may be provided either inline
 * or via a classpath/file location.
 */
final class PemUtils {

    private PemUtils() {
    }

    static String loadPem(String value) {
        if (value == null) {
            throw new IllegalArgumentException("PEM value is null");
        }
        String trimmed = stripEnclosingQuotes(value.trim());
        if (trimmed.isBlank()) {
            throw new IllegalArgumentException("PEM value is blank");
        }

        if (trimmed.startsWith("-----BEGIN")) {
            return trimmed;
        }

        // Try to decode as base64 (for keys stored as base64-encoded environment variables)
        String decodedPem = tryDecodeBase64(trimmed);
        if (decodedPem != null) {
            return decodedPem;
        }

        URL resourceUrl = resolveUrl(trimmed);
        if (resourceUrl != null) {
            try (InputStream inputStream = resourceUrl.openStream()) {
                return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            } catch (IOException ex) {
                throw new IllegalArgumentException("Unable to read PEM resource " + trimmed, ex);
            }
        }

        Path path = Path.of(trimmed);
        if (Files.exists(path)) {
            try {
                return Files.readString(path, StandardCharsets.UTF_8);
            } catch (IOException ex) {
                throw new IllegalArgumentException("Unable to read PEM file " + trimmed, ex);
            }
        }

        throw new IllegalArgumentException("Unable to resolve PEM data, expected inline value or readable resource");
    }

    private static URL resolveUrl(String location) {
        try {
            return ResourceUtils.getURL(location);
        } catch (FileNotFoundException ex) {
            return null;
        }
    }

    private static String stripEnclosingQuotes(String value) {
        if ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'"))) {
            return value.substring(1, value.length() - 1).trim();
        }
        return value;
    }

    private static String tryDecodeBase64(String value) {
        try {
            byte[] decoded = Base64.getMimeDecoder().decode(value);
            String decodedPem = new String(decoded, StandardCharsets.UTF_8);
            if (decodedPem.startsWith("-----BEGIN")) {
                return decodedPem;
            }
        } catch (IllegalArgumentException ignored) {
            // Not valid base64, continue with other resolution strategies
        }
        return null;
    }
}
