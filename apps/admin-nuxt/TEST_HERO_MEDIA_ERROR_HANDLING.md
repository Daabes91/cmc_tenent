# Hero Media Error Handling - Test Guide

## Quick Test Scenarios

### Test 1: Image Upload Success Flow
1. Navigate to Clinic Settings page
2. Scroll to "Hero Media" section
3. Select "Image" as media type
4. Upload a valid image (JPEG/PNG, < 10MB)
5. **Expected:** 
   - Loading overlay appears during upload
   - Success toast: "Image Uploaded - Hero image has been uploaded successfully"
   - Image preview updates
   - Green check icon appears

### Test 2: Image Upload Error Flow
1. Navigate to Clinic Settings page
2. Scroll to "Hero Media" section
3. Select "Image" as media type
4. Try to upload an invalid file (e.g., .txt file)
5. **Expected:**
   - Error toast: "Upload Failed - Failed to upload hero image. Please try again."
   - Upload area remains accessible for retry
   - No state corruption

### Test 3: YouTube URL Validation Success
1. Navigate to Clinic Settings page
2. Scroll to "Hero Media" section
3. Select "Video" as media type
4. Enter valid YouTube URL: `https://www.youtube.com/watch?v=dQw4w9WgXcQ`
5. Click outside the input (blur)
6. **Expected:**
   - Loading spinner appears in input
   - Success toast: "Valid YouTube URL - YouTube video has been validated successfully"
   - Green check icon appears in input
   - Preview updates with video

### Test 4: YouTube URL Validation Error
1. Navigate to Clinic Settings page
2. Scroll to "Hero Media" section
3. Select "Video" as media type
4. Enter invalid URL: `https://example.com/video`
5. Click outside the input (blur)
6. **Expected:**
   - Error toast: "Invalid YouTube URL - Please enter a valid YouTube video URL"
   - Red error message below input: "Please enter a valid YouTube URL"
   - Red alert icon in input
   - Examples section shows valid formats

### Test 5: Clear Media Confirmation
1. Configure any hero media (image or video)
2. Click "Clear Media" button
3. **Expected:**
   - Blue info toast: "Media Cleared - Hero media configuration has been reset"
   - Preview shows default fallback
   - Form resets to default state

### Test 6: Save with Invalid Configuration
1. Select "Video" as media type
2. Leave YouTube URL empty or invalid
3. Click "Save Settings" button
4. **Expected:**
   - Warning toast: "Invalid hero media configuration - Please enter a valid YouTube URL or switch to image mode."
   - Save operation cancelled
   - Form remains editable

### Test 7: Save Success with Hero Media
1. Configure valid hero media (image or video)
2. Click "Save Settings" button
3. **Expected:**
   - Loading toast: "Saving settings... - Please wait while we update your clinic settings"
   - Success toast: "Settings saved - Your clinic settings and hero media have been updated successfully"
   - Page refreshes with new data

### Test 8: Save Error Handling
1. Disconnect network or simulate API error
2. Configure hero media
3. Click "Save Settings" button
4. **Expected:**
   - Loading toast appears
   - Error toast: "Hero media save failed - [Error message]"
   - Form data preserved
   - Can retry save

### Test 9: Loading States
1. Upload large image (close to 10MB)
2. **Expected:**
   - Semi-transparent overlay on upload area
   - Animated spinner
   - "Uploading image..." text
   - Upload controls disabled
   - Loading state clears on completion

### Test 10: Retry Mechanisms
1. Trigger any error (upload, validation, save)
2. Attempt to retry the operation
3. **Expected:**
   - Error state clears
   - Operation can be retried immediately
   - No page reload required
   - Form data preserved

## Accessibility Tests

### Keyboard Navigation
1. Tab through all form elements
2. **Expected:**
   - All inputs accessible via keyboard
   - Focus indicators visible
   - Tab order logical
   - Can trigger all actions with keyboard

### Screen Reader
1. Use screen reader (VoiceOver/NVDA)
2. Navigate through hero media section
3. **Expected:**
   - All labels announced
   - Error messages announced
   - Toast notifications announced
   - Loading states communicated

## Internationalization Tests

### English (en)
1. Set language to English
2. Trigger all error/success scenarios
3. **Expected:**
   - All messages in English
   - Proper grammar and spelling
   - Clear, concise messaging

### Arabic (ar)
1. Set language to Arabic
2. Trigger all error/success scenarios
3. **Expected:**
   - All messages in Arabic
   - RTL layout correct
   - Culturally appropriate messaging
   - Toast positioning correct (left side)

## Performance Tests

### Toast Timeout Verification
- Success toasts: Auto-dismiss after 3 seconds
- Error toasts: Auto-dismiss after 8 seconds
- Warning toasts: Auto-dismiss after 5 seconds
- Loading toasts: Manual dismiss only

### State Management
- No memory leaks from unclosed toasts
- Proper cleanup on component unmount
- Efficient re-renders during state changes

## Browser Compatibility

Test in:
- [ ] Chrome (latest)
- [ ] Firefox (latest)
- [ ] Safari (latest)
- [ ] Edge (latest)
- [ ] Mobile Safari (iOS)
- [ ] Chrome Mobile (Android)

## Test Results Template

```
Test Date: ___________
Tester: ___________
Browser: ___________
Language: ___________

| Test # | Scenario | Pass/Fail | Notes |
|--------|----------|-----------|-------|
| 1 | Image Upload Success | ☐ | |
| 2 | Image Upload Error | ☐ | |
| 3 | YouTube Validation Success | ☐ | |
| 4 | YouTube Validation Error | ☐ | |
| 5 | Clear Media | ☐ | |
| 6 | Save Invalid Config | ☐ | |
| 7 | Save Success | ☐ | |
| 8 | Save Error | ☐ | |
| 9 | Loading States | ☐ | |
| 10 | Retry Mechanisms | ☐ | |

Overall Result: ☐ Pass ☐ Fail
```

## Known Issues

None at this time.

## Notes

- All toast notifications use the standard Nuxt UI toast system
- Error messages are user-friendly and actionable
- Loading states prevent concurrent operations
- Retry mechanisms work without page reload
- All translations are complete in English and Arabic
