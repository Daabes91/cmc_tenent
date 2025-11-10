# Hero Section Error Handling Test Guide

This document provides a comprehensive testing guide for the hero section error handling and fallback mechanisms implemented in Task 8.

## Test Overview

The hero section now includes:
1. Error boundary component to catch React errors
2. YouTube video error handling with timeout detection
3. Image load error handling with fallback
4. Comprehensive console logging for debugging
5. Graceful degradation to ensure page continues to render

## Components Tested

- `HeroSectionErrorBoundary.tsx` - Error boundary wrapper
- `YouTubeEmbed.tsx` - Enhanced with error handling and timeout
- `HomePageClient.tsx` - Integrated error handling

## Test Scenarios

### 1. YouTube Video Load Success
**Expected Behavior:**
- Video loads and plays automatically
- Loading indicator shows briefly then disappears
- Console logs: "YouTube video loaded successfully: {videoId}"
- No errors displayed to user

**How to Test:**
1. Configure a valid YouTube video ID in clinic settings
2. Navigate to the home page
3. Verify video loads and plays
4. Check browser console for success log

### 2. YouTube Video Load Failure (Invalid Video ID)
**Expected Behavior:**
- Video fails to load
- System falls back to default image
- Console logs error with details
- Page continues to render normally

**How to Test:**
1. Configure an invalid YouTube video ID (e.g., "invalid123")
2. Navigate to the home page
3. Verify default image is displayed
4. Check console for error: "Failed to load YouTube video"

### 3. YouTube Video Load Timeout
**Expected Behavior:**
- Video takes too long to load (>10 seconds)
- Timeout triggers fallback to default image
- Console logs timeout error
- Page remains functional

**How to Test:**
1. Use browser DevTools to throttle network to "Slow 3G"
2. Configure a valid YouTube video ID
3. Navigate to the home page
4. Wait 10+ seconds
5. Verify fallback to default image occurs
6. Check console for: "YouTube video load timeout"

### 4. Custom Image Load Success
**Expected Behavior:**
- Custom image loads and displays
- Gradient overlay applied
- No errors in console

**How to Test:**
1. Configure a valid custom image URL in clinic settings
2. Set media type to "image"
3. Navigate to the home page
4. Verify custom image displays correctly

### 5. Custom Image Load Failure
**Expected Behavior:**
- Custom image fails to load
- System falls back to default image
- Console logs error
- Page continues to render

**How to Test:**
1. Configure an invalid image URL (e.g., "https://invalid.url/image.jpg")
2. Set media type to "image"
3. Navigate to the home page
4. Verify default image is displayed
5. Check console for: "Hero image failed to load, falling back to default"

### 6. API Settings Load Failure
**Expected Behavior:**
- API call to get clinic settings fails
- System falls back to default image
- Console logs error with details
- Page continues to render normally

**How to Test:**
1. Use browser DevTools Network tab to block API requests
2. Navigate to the home page
3. Verify default image is displayed
4. Check console for: "Failed to load hero media settings"

### 7. React Error in Hero Section
**Expected Behavior:**
- Error boundary catches the error
- Fallback UI (default image) is displayed
- Error logged to console
- Rest of page continues to work

**How to Test:**
1. Temporarily modify code to throw an error in hero section
2. Navigate to the home page
3. Verify error boundary shows fallback image
4. Check console for: "Hero section error boundary caught error"

### 8. No Custom Media Configured
**Expected Behavior:**
- Default image is displayed
- No errors in console
- Console logs: "No custom hero media configured, using default image"

**How to Test:**
1. Clear all hero media settings in clinic settings
2. Navigate to the home page
3. Verify default image displays
4. Check console for default image log

## Console Logging Reference

### Success Logs
```
Hero media settings loaded: { heroMediaType, hasVideoId, hasImageUrl, timestamp }
YouTube video loaded successfully: {videoId}
No custom hero media configured, using default image
```

### Error Logs
```
Failed to load hero media settings: { error, stack, timestamp }
Failed to load YouTube video: {videoId} { reason, timestamp, videoId }
YouTube video load timeout: {videoId} - Video may be unavailable or restricted
Hero image failed to load, falling back to default: { attemptedUrl, timestamp }
Hero section error boundary caught error: {error}
YouTubeEmbed returning null due to error, parent should show fallback
```

## Automated Testing Commands

Run the following commands to verify no TypeScript errors:

```bash
cd apps/web-next
npm run type-check
```

## Manual Testing Checklist

- [ ] Test with valid YouTube video
- [ ] Test with invalid YouTube video ID
- [ ] Test with slow network (video timeout)
- [ ] Test with valid custom image
- [ ] Test with invalid custom image URL
- [ ] Test with API failure (network blocked)
- [ ] Test with no custom media configured
- [ ] Test error boundary (inject error)
- [ ] Verify all console logs are present
- [ ] Verify page continues to render in all scenarios
- [ ] Test on mobile devices
- [ ] Test in different browsers (Chrome, Firefox, Safari)
- [ ] Test in light and dark mode

## Expected Results Summary

All error scenarios should:
1. ✅ Display default fallback image
2. ✅ Log detailed error information to console
3. ✅ Continue rendering the page normally
4. ✅ Not show error messages to end users
5. ✅ Maintain responsive design
6. ✅ Work in both light and dark modes

## Debugging Tips

1. **Check Browser Console**: All errors are logged with timestamps and context
2. **Network Tab**: Monitor API calls and resource loading
3. **React DevTools**: Inspect component state and props
4. **Throttle Network**: Test timeout scenarios with slow connections
5. **Block Requests**: Use DevTools to simulate API failures

## Success Criteria

✅ All error scenarios handled gracefully
✅ Default image always displays as fallback
✅ No user-facing error messages
✅ Comprehensive console logging for debugging
✅ Page continues to render normally
✅ No TypeScript errors
✅ Responsive design maintained
✅ Works across all browsers and devices
