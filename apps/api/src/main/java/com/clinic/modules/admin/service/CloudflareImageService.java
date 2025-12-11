package com.clinic.modules.admin.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class CloudflareImageService {

    private static final Logger log = LoggerFactory.getLogger(CloudflareImageService.class);

    private final RestTemplate restTemplate;

    @Value("${security.cloudflare.images.account-id}")
    private String accountId;

    @Value("${security.cloudflare.images.api-token}")
    private String apiToken;

    @Value("${security.cloudflare.images.delivery-url}")
    private String deliveryUrl;

    public CloudflareImageService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public CloudflareImageUploadResponse uploadImage(MultipartFile file) throws IOException {
        String url = String.format("https://api.cloudflare.com/client/v4/accounts/%s/images/v1", accountId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.setBearerAuth(apiToken);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        });

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                Boolean success = (Boolean) responseBody.get("success");
                
                if (Boolean.TRUE.equals(success)) {
                    Map<String, Object> result = (Map<String, Object>) responseBody.get("result");
                    String imageId = (String) result.get("id");
                    List<String> variants = (List<String>) result.get("variants");
                    String publicUrl = variants != null && !variants.isEmpty() ? variants.get(0) : null;
                    
                    log.info("Successfully uploaded image to Cloudflare: {}", imageId);
                    
                    return new CloudflareImageUploadResponse(true, imageId, publicUrl, deliveryUrl);
                } else {
                    List<Map<String, Object>> errors = (List<Map<String, Object>>) responseBody.get("errors");
                    String errorMessage = errors != null && !errors.isEmpty() 
                            ? (String) errors.get(0).get("message") 
                            : "Unknown error";
                    log.error("Cloudflare API returned error: {}", errorMessage);
                    throw new RuntimeException("Failed to upload image: " + errorMessage);
                }
            }
            
            throw new RuntimeException("Unexpected response from Cloudflare API");
        } catch (Exception e) {
            log.error("Error uploading image to Cloudflare", e);
            throw new RuntimeException("Failed to upload image to Cloudflare: " + e.getMessage(), e);
        }
    }

    public static class CloudflareImageUploadResponse {
        private final boolean success;
        private final String imageId;
        private final String url;
        private final String deliveryUrl;

        public CloudflareImageUploadResponse(boolean success, String imageId, String url, String deliveryUrl) {
            this.success = success;
            this.imageId = imageId;
            this.url = url;
            this.deliveryUrl = deliveryUrl;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getImageId() {
            return imageId;
        }

        public String getUrl() {
            return url;
        }

        public String getDeliveryUrl() {
            return deliveryUrl;
        }
    }
}
