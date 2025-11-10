# Hero Media Customization - Backend Implementation Summary

## Overview
This document summarizes the backend implementation for the hero media customization feature, which allows clinic administrators to customize the hero section background media on their landing page.

## Changes Implemented

### 1. Database Schema Migration
**File:** `apps/api/src/main/resources/db/migration/V30__add_hero_media_fields.sql`

Added three new columns to the `clinic_settings` table:
- `hero_media_type` (VARCHAR(10), default 'image'): Type of hero media (image or video)
- `hero_image_url` (TEXT): Full URL to the uploaded hero image from Cloudflare Images
- `hero_video_id` (VARCHAR(20)): YouTube video ID extracted from the URL

### 2. Entity Updates
**File:** `apps/api/src/main/java/com/clinic/modules/core/settings/ClinicSettingsEntity.java`

Added three new fields with getters and setters:
- `heroMediaType`: String field for media type
- `heroImageUrl`: String field for image URL
- `heroVideoId`: String field for YouTube video ID

### 3. YouTube URL Validation Utility
**File:** `apps/api/src/main/java/com/clinic/util/YouTubeUrlValidator.java`

Created a comprehensive utility class for YouTube URL validation:
- `isValidYouTubeUrl(String url)`: Validates if a URL is a valid YouTube URL
- `extractVideoId(String url)`: Extracts the video ID from various YouTube URL formats
- `validateAndExtract(String url)`: Validates and extracts video ID with detailed error messages

**Supported URL Formats:**
- `https://www.youtube.com/watch?v=VIDEO_ID`
- `https://youtu.be/VIDEO_ID`
- `https://www.youtube.com/embed/VIDEO_ID`
- URLs with additional query parameters (e.g., `?t=10s`)

### 4. DTO Updates

#### ClinicSettingsResponse
**File:** `apps/api/src/main/java/com/clinic/modules/admin/dto/ClinicSettingsResponse.java`

Added three new fields to the response:
- `heroMediaType`: String
- `heroImageUrl`: String
- `heroVideoId`: String

#### ClinicSettingsUpdateRequest
**File:** `apps/api/src/main/java/com/clinic/modules/admin/dto/ClinicSettingsUpdateRequest.java`

Added three new fields to the update request:
- `heroMediaType`: String
- `heroImageUrl`: String
- `heroVideoId`: String

### 5. Service Layer Updates
**File:** `apps/api/src/main/java/com/clinic/modules/admin/service/ClinicSettingsService.java`

Updated the `updateSettings` method to handle hero media fields:
- Validates and normalizes `heroMediaType` (only accepts 'image' or 'video')
- Stores `heroImageUrl` as provided
- Validates `heroVideoId` - if a full YouTube URL is provided, extracts the video ID automatically
- Updated `mapToResponse` method to include hero media fields in the response

### 6. Test Coverage
**File:** `apps/api/src/test/java/com/clinic/util/YouTubeUrlValidatorTest.java`

Created comprehensive unit tests for the YouTube URL validator:
- Tests for valid YouTube URLs in various formats
- Tests for invalid URLs
- Tests for video ID extraction
- Tests for validation with error messages

## API Endpoints

The existing clinic settings endpoints automatically support the new fields:

### GET /admin/settings
Returns clinic settings including hero media configuration.

**Response includes:**
```json
{
  "heroMediaType": "image",
  "heroImageUrl": "https://imagedelivery.net/...",
  "heroVideoId": null
}
```

### PUT /admin/settings
Updates clinic settings including hero media configuration.

**Request body can include:**
```json
{
  "heroMediaType": "video",
  "heroImageUrl": null,
  "heroVideoId": "dQw4w9WgXcQ"
}
```

Or with a full YouTube URL (will be automatically converted to video ID):
```json
{
  "heroMediaType": "video",
  "heroVideoId": "https://www.youtube.com/watch?v=dQw4w9WgXcQ"
}
```

## Validation Rules

1. **Media Type**: Must be either "image" or "video"
2. **Image URL**: No specific validation (relies on Cloudflare Images upload)
3. **Video ID**: 
   - Can be provided as a YouTube video ID (11 characters)
   - Can be provided as a full YouTube URL (will be automatically extracted)
   - Must match YouTube URL format if provided as URL

## Database Migration

To apply the migration:
```bash
./gradlew flywayMigrate
```

The migration is backward compatible - existing clinic settings will default to:
- `hero_media_type`: 'image'
- `hero_image_url`: NULL
- `hero_video_id`: NULL

## Build Verification

All changes have been compiled and verified:
```bash
./gradlew clean build -x test
# BUILD SUCCESSFUL
```

## Next Steps

The backend is now ready for:
1. Frontend admin panel integration (Task 2-7)
2. Frontend landing page integration (Task 8-9)
3. Translation updates (Task 10)
4. Performance optimization (Task 11)
5. Documentation (Task 12)

## Requirements Satisfied

This implementation satisfies the following requirements from the spec:
- **Requirement 5.1**: Store hero media type in clinic settings ✓
- **Requirement 5.2**: Store image URL where media type is image ✓
- **Requirement 5.3**: Store YouTube video ID where media type is video ✓
