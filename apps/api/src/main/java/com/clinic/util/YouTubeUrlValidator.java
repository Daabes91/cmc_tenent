package com.clinic.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for validating YouTube URLs and extracting video IDs.
 */
public class YouTubeUrlValidator {

    // Regex pattern to match various YouTube URL formats
    private static final Pattern YOUTUBE_PATTERN = Pattern.compile(
            "^(?:https?://)?(?:www\\.)?" +
            "(?:youtube\\.com/watch\\?v=|youtu\\.be/|youtube\\.com/embed/)" +
            "([a-zA-Z0-9_-]{11})" +
            "(?:[?&].*)?$"
    );

    /**
     * Validates if the given URL is a valid YouTube URL.
     *
     * @param url the URL to validate
     * @return true if the URL is a valid YouTube URL, false otherwise
     */
    public static boolean isValidYouTubeUrl(String url) {
        if (url == null || url.isBlank()) {
            return false;
        }
        return YOUTUBE_PATTERN.matcher(url.trim()).matches();
    }

    /**
     * Extracts the YouTube video ID from a valid YouTube URL.
     *
     * @param url the YouTube URL
     * @return the video ID if found, null otherwise
     */
    public static String extractVideoId(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }

        Matcher matcher = YOUTUBE_PATTERN.matcher(url.trim());
        if (matcher.matches()) {
            return matcher.group(1);
        }
        return null;
    }

    /**
     * Validates and extracts the video ID from a YouTube URL.
     *
     * @param url the YouTube URL
     * @return a ValidationResult containing the video ID if valid, or an error message
     */
    public static ValidationResult validateAndExtract(String url) {
        if (url == null || url.isBlank()) {
            return new ValidationResult(false, null, "YouTube URL cannot be empty");
        }

        String videoId = extractVideoId(url);
        if (videoId == null) {
            return new ValidationResult(false, null, 
                "Invalid YouTube URL format. Expected formats: " +
                "https://www.youtube.com/watch?v=VIDEO_ID, " +
                "https://youtu.be/VIDEO_ID, or " +
                "https://www.youtube.com/embed/VIDEO_ID");
        }

        return new ValidationResult(true, videoId, null);
    }

    /**
     * Result of YouTube URL validation.
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String videoId;
        private final String errorMessage;

        public ValidationResult(boolean valid, String videoId, String errorMessage) {
            this.valid = valid;
            this.videoId = videoId;
            this.errorMessage = errorMessage;
        }

        public boolean isValid() {
            return valid;
        }

        public String getVideoId() {
            return videoId;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }
}
