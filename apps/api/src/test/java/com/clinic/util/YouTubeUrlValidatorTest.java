package com.clinic.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class YouTubeUrlValidatorTest {

    @Test
    void testValidYouTubeUrls() {
        // Standard watch URL
        assertTrue(YouTubeUrlValidator.isValidYouTubeUrl("https://www.youtube.com/watch?v=dQw4w9WgXcQ"));
        assertTrue(YouTubeUrlValidator.isValidYouTubeUrl("http://www.youtube.com/watch?v=dQw4w9WgXcQ"));
        assertTrue(YouTubeUrlValidator.isValidYouTubeUrl("www.youtube.com/watch?v=dQw4w9WgXcQ"));
        assertTrue(YouTubeUrlValidator.isValidYouTubeUrl("youtube.com/watch?v=dQw4w9WgXcQ"));

        // Short URL
        assertTrue(YouTubeUrlValidator.isValidYouTubeUrl("https://youtu.be/dQw4w9WgXcQ"));
        assertTrue(YouTubeUrlValidator.isValidYouTubeUrl("http://youtu.be/dQw4w9WgXcQ"));
        assertTrue(YouTubeUrlValidator.isValidYouTubeUrl("youtu.be/dQw4w9WgXcQ"));

        // Embed URL
        assertTrue(YouTubeUrlValidator.isValidYouTubeUrl("https://www.youtube.com/embed/dQw4w9WgXcQ"));
        assertTrue(YouTubeUrlValidator.isValidYouTubeUrl("youtube.com/embed/dQw4w9WgXcQ"));

        // With additional parameters
        assertTrue(YouTubeUrlValidator.isValidYouTubeUrl("https://www.youtube.com/watch?v=dQw4w9WgXcQ&t=10s"));
        assertTrue(YouTubeUrlValidator.isValidYouTubeUrl("https://youtu.be/dQw4w9WgXcQ?t=10"));
    }

    @Test
    void testInvalidYouTubeUrls() {
        assertFalse(YouTubeUrlValidator.isValidYouTubeUrl(null));
        assertFalse(YouTubeUrlValidator.isValidYouTubeUrl(""));
        assertFalse(YouTubeUrlValidator.isValidYouTubeUrl("   "));
        assertFalse(YouTubeUrlValidator.isValidYouTubeUrl("https://vimeo.com/123456"));
        assertFalse(YouTubeUrlValidator.isValidYouTubeUrl("https://www.youtube.com/watch"));
        assertFalse(YouTubeUrlValidator.isValidYouTubeUrl("https://www.youtube.com/watch?v="));
        assertFalse(YouTubeUrlValidator.isValidYouTubeUrl("https://www.youtube.com/watch?v=short"));
        assertFalse(YouTubeUrlValidator.isValidYouTubeUrl("not a url"));
    }

    @Test
    void testExtractVideoId() {
        // Standard watch URL
        assertEquals("dQw4w9WgXcQ", YouTubeUrlValidator.extractVideoId("https://www.youtube.com/watch?v=dQw4w9WgXcQ"));
        assertEquals("dQw4w9WgXcQ", YouTubeUrlValidator.extractVideoId("http://www.youtube.com/watch?v=dQw4w9WgXcQ"));
        assertEquals("dQw4w9WgXcQ", YouTubeUrlValidator.extractVideoId("youtube.com/watch?v=dQw4w9WgXcQ"));

        // Short URL
        assertEquals("dQw4w9WgXcQ", YouTubeUrlValidator.extractVideoId("https://youtu.be/dQw4w9WgXcQ"));
        assertEquals("dQw4w9WgXcQ", YouTubeUrlValidator.extractVideoId("youtu.be/dQw4w9WgXcQ"));

        // Embed URL
        assertEquals("dQw4w9WgXcQ", YouTubeUrlValidator.extractVideoId("https://www.youtube.com/embed/dQw4w9WgXcQ"));

        // With additional parameters
        assertEquals("dQw4w9WgXcQ", YouTubeUrlValidator.extractVideoId("https://www.youtube.com/watch?v=dQw4w9WgXcQ&t=10s"));
        assertEquals("dQw4w9WgXcQ", YouTubeUrlValidator.extractVideoId("https://youtu.be/dQw4w9WgXcQ?t=10"));

        // Invalid URLs
        assertNull(YouTubeUrlValidator.extractVideoId(null));
        assertNull(YouTubeUrlValidator.extractVideoId(""));
        assertNull(YouTubeUrlValidator.extractVideoId("not a url"));
        assertNull(YouTubeUrlValidator.extractVideoId("https://vimeo.com/123456"));
    }

    @Test
    void testValidateAndExtract() {
        // Valid URL
        YouTubeUrlValidator.ValidationResult result = YouTubeUrlValidator.validateAndExtract("https://www.youtube.com/watch?v=dQw4w9WgXcQ");
        assertTrue(result.isValid());
        assertEquals("dQw4w9WgXcQ", result.getVideoId());
        assertNull(result.getErrorMessage());

        // Invalid URL
        result = YouTubeUrlValidator.validateAndExtract("https://vimeo.com/123456");
        assertFalse(result.isValid());
        assertNull(result.getVideoId());
        assertNotNull(result.getErrorMessage());
        assertTrue(result.getErrorMessage().contains("Invalid YouTube URL format"));

        // Empty URL
        result = YouTubeUrlValidator.validateAndExtract("");
        assertFalse(result.isValid());
        assertNull(result.getVideoId());
        assertNotNull(result.getErrorMessage());
        assertTrue(result.getErrorMessage().contains("cannot be empty"));

        // Null URL
        result = YouTubeUrlValidator.validateAndExtract(null);
        assertFalse(result.isValid());
        assertNull(result.getVideoId());
        assertNotNull(result.getErrorMessage());
    }
}
