package com.clinic.modules.core.image;

import com.clinic.config.CloudflareImageProperties;
import com.clinic.modules.core.settings.ClinicSettingsRepository;
import com.clinic.modules.core.tenant.TenantContextHolder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Base64;



/**
 * Service for uploading images to Cloudflare Images API.
 * Reads credentials from database settings first, falls back to environment variables.
 */
@Service
public class ImageUploadService {

    private static final Logger log = LoggerFactory.getLogger(ImageUploadService.class);

    private final CloudflareImageProperties cloudflareProperties;
    private final ClinicSettingsRepository settingsRepository;
    private final TenantContextHolder tenantContextHolder;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public ImageUploadService(
            CloudflareImageProperties cloudflareProperties,
            ClinicSettingsRepository settingsRepository,
            RestTemplate restTemplate,
            ObjectMapper objectMapper,
            TenantContextHolder tenantContextHolder) {
        this.cloudflareProperties = cloudflareProperties;
        this.settingsRepository = settingsRepository;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.tenantContextHolder = tenantContextHolder;
    }





    /**
     * Upload an image to Cloudflare Images with variants.
     *
     * @param file The image file to upload
     * @param metadata Optional metadata for the image
     * @param requireSignedUrls Whether to require signed URLs for access
     * @return ImageUploadResponse containing the image ID and URLs
     * @throws ImageUploadException if upload fails
     */
    public ImageUploadResponse uploadImage(MultipartFile file, String metadata, boolean requireSignedUrls) throws ImageUploadException {
        validateFile(file);

        try {
            if (cloudflareProperties.isMockMode()) {
                log.warn("Cloudflare mockMode enabled - returning data URI instead of uploading");
                return createMockUploadResponse(file);
            }

            CloudflareCredentials credentials = getCloudflareCredentials();

            String uploadUrl = String.format("%s/accounts/%s/images/v1",
                    cloudflareProperties.getBaseUrl(),
                    credentials.accountId());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.setBearerAuth(credentials.apiToken());

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", file.getResource());
            if (metadata != null && !metadata.isEmpty()) {
                body.add("metadata", metadata);
            }
            body.add("requireSignedURLs", String.valueOf(requireSignedUrls));

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            log.info("Uploading image to Cloudflare: filename={}, size={} bytes",
                    file.getOriginalFilename(), file.getSize());

            ResponseEntity<String> response = uploadWithRetry(uploadUrl, requestEntity);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return parseUploadResponse(response.getBody());
            } else {
                throw new ImageUploadException("Failed to upload image: " + response.getStatusCode());
            }

        } catch (Exception e) {
            // Fall back only if mock mode is enabled; otherwise surface the error
            if (cloudflareProperties.isMockMode() && isAuthOrConnectivityError(e)) {
                log.warn("Cloudflare upload failed (auth/connectivity). Mock mode enabled, returning mock upload: {}", e.getMessage());
                return createMockUploadResponse(file);
            }

            log.error("Error uploading image to Cloudflare", e);
            throw new ImageUploadException("Failed to upload image: " + e.getMessage(), e);
        }
    }

    /**
     * Upload an image to Cloudflare Images (convenience method with default settings).
     */
    public ImageUploadResponse uploadImage(MultipartFile file, String metadata) throws ImageUploadException {
        return uploadImage(file, metadata, false);
    }

    /**
     * Upload multiple images in bulk.
     *
     * @param files List of image files to upload
     * @param metadata Optional metadata for all images
     * @return List of ImageUploadResponse containing results for each upload
     */
    public BulkUploadResponse uploadImages(List<MultipartFile> files, String metadata) {
        List<ImageUploadResult> results = new ArrayList<>();
        int successCount = 0;
        int failureCount = 0;

        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            try {
                ImageUploadResponse response = uploadImage(file, metadata, false);
                results.add(new ImageUploadResult(i, file.getOriginalFilename(), true, null, response));
                successCount++;
                log.info("Bulk upload success: {} ({}/{})", file.getOriginalFilename(), i + 1, files.size());
            } catch (ImageUploadException e) {
                results.add(new ImageUploadResult(i, file.getOriginalFilename(), false, e.getMessage(), null));
                failureCount++;
                log.error("Bulk upload failed: {} ({}/{}): {}", file.getOriginalFilename(), i + 1, files.size(), e.getMessage());
            }
        }

        return new BulkUploadResponse(results, successCount, failureCount);
    }

    /**
     * Get image variants for different use cases.
     *
     * @param imageId The Cloudflare image ID
     * @return Map of variant names to URLs
     */
    public Map<String, String> getImageVariants(String imageId) {
        if (cloudflareProperties.isMockMode()) {
            return Collections.emptyMap();
        }

        Map<String, String> variants = new HashMap<>();
        String baseUrl = cloudflareProperties.getDeliveryUrl();
        int quality = cloudflareProperties.getImageQuality();
        
        // Standard variants for clinic use with optimization
        variants.put("thumbnail", String.format("%s/%s/w=150,h=150,fit=crop,quality=%d", baseUrl, imageId, quality));
        variants.put("small", String.format("%s/%s/w=300,h=300,fit=scale-down,quality=%d", baseUrl, imageId, quality));
        variants.put("medium", String.format("%s/%s/w=600,h=600,fit=scale-down,quality=%d", baseUrl, imageId, quality));
        variants.put("large", String.format("%s/%s/w=1200,h=1200,fit=scale-down,quality=%d", baseUrl, imageId, quality));
        variants.put("hero", String.format("%s/%s/w=1920,h=1080,fit=crop,quality=%d", baseUrl, imageId, quality));
        variants.put("public", String.format("%s/%s/public", baseUrl, imageId));
        
        // WebP variants for better compression
        if (cloudflareProperties.isAutoOptimize()) {
            variants.put("thumbnail_webp", String.format("%s/%s/w=150,h=150,fit=crop,quality=%d,format=webp", baseUrl, imageId, quality));
            variants.put("small_webp", String.format("%s/%s/w=300,h=300,fit=scale-down,quality=%d,format=webp", baseUrl, imageId, quality));
            variants.put("medium_webp", String.format("%s/%s/w=600,h=600,fit=scale-down,quality=%d,format=webp", baseUrl, imageId, quality));
            variants.put("large_webp", String.format("%s/%s/w=1200,h=1200,fit=scale-down,quality=%d,format=webp", baseUrl, imageId, quality));
        }
        
        return variants;
    }

    /**
     * Test connectivity to Cloudflare Images API.
     *
     * @return true if the API is reachable, false otherwise
     */
    public boolean testConnectivity() {
        if (cloudflareProperties.isMockMode()) {
            return true;
        }

        try {
            String testUrl = String.format("%s/accounts/%s/images/v1",
                    cloudflareProperties.getBaseUrl(),
                    cloudflareProperties.getAccountId());

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(cloudflareProperties.getApiToken());

            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    testUrl,
                    HttpMethod.GET,
                    requestEntity,
                    String.class
            );

            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.warn("Cloudflare connectivity test failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Delete an image from Cloudflare Images.
     *
     * @param imageId The ID of the image to delete
     * @throws ImageUploadException if deletion fails
     */
    public void deleteImage(String imageId) throws ImageUploadException {
        try {
            String deleteUrl = String.format("%s/accounts/%s/images/v1/%s",
                    cloudflareProperties.getBaseUrl(),
                    cloudflareProperties.getAccountId(),
                    imageId);

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(cloudflareProperties.getApiToken());

            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

            log.info("Deleting image from Cloudflare: imageId={}", imageId);

            ResponseEntity<String> response = restTemplate.exchange(
                    deleteUrl,
                    HttpMethod.DELETE,
                    requestEntity,
                    String.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new ImageUploadException("Failed to delete image: " + response.getStatusCode());
            }

            log.info("Successfully deleted image: imageId={}", imageId);

        } catch (Exception e) {
            log.error("Error deleting image from Cloudflare: imageId={}", imageId, e);
            throw new ImageUploadException("Failed to delete image: " + e.getMessage(), e);
        }
    }

    private void validateFile(MultipartFile file) throws ImageUploadException {
        if (file == null || file.isEmpty()) {
            throw new ImageUploadException("File is empty or null");
        }

        // Check file size
        if (file.getSize() > cloudflareProperties.getMaxFileSizeBytes()) {
            throw new ImageUploadException(
                    String.format("File size exceeds maximum allowed size of %d MB",
                            cloudflareProperties.getMaxFileSizeMb())
            );
        }

        // Check file extension
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !hasValidExtension(originalFilename)) {
            throw new ImageUploadException(
                    "Invalid file type. Allowed types: " + cloudflareProperties.getAllowedExtensions()
            );
        }

        // Check content type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new ImageUploadException("File must be an image");
        }
    }

    private boolean hasValidExtension(String filename) {
        String extension = getFileExtension(filename).toLowerCase();
        return cloudflareProperties.getAllowedExtensionsList().contains(extension);
    }

    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1);
    }



    private ResponseEntity<String> uploadWithRetry(String uploadUrl, HttpEntity<MultiValueMap<String, Object>> requestEntity) throws ImageUploadException {
        int maxRetries = 3;
        int retryDelay = 2000; // 2 seconds
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                log.info("Upload attempt {} of {}", attempt, maxRetries);
                return restTemplate.exchange(
                        uploadUrl,
                        HttpMethod.POST,
                        requestEntity,
                        String.class
                );
            } catch (Exception e) {
                log.warn("Upload attempt {} failed: {}", attempt, e.getMessage());
                
                if (attempt == maxRetries) {
                    throw new ImageUploadException("Failed to upload after " + maxRetries + " attempts: " + e.getMessage(), e);
                }
                
                try {
                    Thread.sleep(retryDelay);
                    retryDelay *= 2; // Exponential backoff
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new ImageUploadException("Upload interrupted", ie);
                }
            }
        }
        
        throw new ImageUploadException("Unexpected error in retry logic");
    }

    private ImageUploadResponse parseUploadResponse(String responseBody) throws IOException, ImageUploadException {
        JsonNode root = objectMapper.readTree(responseBody);

        if (!root.path("success").asBoolean()) {
            String errorMessage = root.path("errors").toString();
            throw new ImageUploadException("Cloudflare API returned error: " + errorMessage);
        }

        JsonNode result = root.path("result");

        String id = result.path("id").asText();
        String filename = result.path("filename").asText();

        // Construct delivery URLs
        JsonNode variants = result.path("variants");
        String publicUrl = variants.isArray() && variants.size() > 0
                ? variants.get(0).asText()
                : String.format("%s/%s/public", cloudflareProperties.getDeliveryUrl(), id);

        // Generate image variants
        Map<String, String> imageVariants = getImageVariants(id);

        return new ImageUploadResponse(id, filename, publicUrl, imageVariants);
    }

    private ImageUploadResponse createMockUploadResponse(MultipartFile file) throws ImageUploadException {
        try {
            byte[] bytes = file.getBytes();
            String mimeType = Optional.ofNullable(file.getContentType())
                    .filter(ct -> ct.startsWith("image/"))
                    .orElse("image/png");
            String base64 = Base64.getEncoder().encodeToString(bytes);
            String dataUri = "data:" + mimeType + ";base64," + base64;
            String id = UUID.randomUUID().toString();
            Map<String, String> variants = Map.of("public", dataUri);
            return new ImageUploadResponse(id, file.getOriginalFilename(), dataUri, variants);
        } catch (IOException e) {
            throw new ImageUploadException("Failed to create mock upload response", e);
        }
    }

    private boolean isAuthOrConnectivityError(Exception e) {
        String msg = e.getMessage() != null ? e.getMessage().toLowerCase(Locale.ROOT) : "";
        return msg.contains("unable to authenticate") ||
                msg.contains("unauthorized") ||
                msg.contains("forbidden") ||
                msg.contains("connect timed out") ||
                msg.contains("i/o error");
    }

    private String normalize(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String mask(String value) {
        if (value == null || value.length() < 6) {
            return "****";
        }
        return "****" + value.substring(value.length() - 4);
    }



    /**
     * Response object for image upload operations.
     */
    public record ImageUploadResponse(
            String imageId,
            String filename,
            String publicUrl,
            Map<String, String> variants
    ) {
        public ImageUploadResponse(String imageId, String filename, String publicUrl) {
            this(imageId, filename, publicUrl, Map.of());
        }
    }

    /**
     * Response object for bulk upload operations.
     */
    public record BulkUploadResponse(
            List<ImageUploadResult> results,
            int successCount,
            int failureCount
    ) {}

    /**
     * Get Cloudflare credentials from global configuration (single set for all tenants).
     */
    private CloudflareCredentials getCloudflareCredentials() throws ImageUploadException {
        String envAccountId = normalize(cloudflareProperties.getAccountId());
        String envApiToken = normalize(cloudflareProperties.getApiToken());

        if (envAccountId == null || envAccountId.isBlank() ||
            envApiToken == null || envApiToken.isBlank()) {
            throw new ImageUploadException(
                "Cloudflare credentials not configured. Please set CLOUDFLARE_ACCOUNT_ID and CLOUDFLARE_API_TOKEN (or SECURITY_CLOUDFLARE_IMAGES_* equivalents).");
        }

        log.info("Using global Cloudflare credentials (account suffix: {}, baseUrl: {}, deliveryUrl: {}, mockMode: {})",
                mask(envAccountId),
                cloudflareProperties.getBaseUrl(),
                cloudflareProperties.getDeliveryUrl(),
                cloudflareProperties.isMockMode());
        return new CloudflareCredentials(envAccountId, envApiToken);
    }

    /**
     * Cloudflare API credentials.
     */
    private record CloudflareCredentials(String accountId, String apiToken) {}

    /**
     * Individual result for bulk upload operations.
     */
    public record ImageUploadResult(
            int index,
            String filename,
            boolean success,
            String errorMessage,
            ImageUploadResponse response
    ) {}

    /**
     * Exception thrown when image upload operations fail.
     */
    public static class ImageUploadException extends Exception {
        public ImageUploadException(String message) {
            super(message);
        }

        public ImageUploadException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
