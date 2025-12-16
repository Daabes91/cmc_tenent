# Dashboard Contact API Integration Test

## Overview
This test verifies that the "Need Help?" section in the dashboard page now fetches contact information dynamically from the API instead of using hardcoded values.

## Test Cases

### 1. API Data Loading
- **Expected**: Contact section shows loading state while fetching clinic settings
- **Verify**: Loading skeleton appears with animated placeholders

### 2. Dynamic Phone Number
- **Expected**: Phone number comes from `clinicSettings.phone` API response
- **Verify**: Phone number is clickable with `tel:` link
- **Fallback**: Shows translation value if API data unavailable

### 3. Dynamic Email
- **Expected**: Email comes from `clinicSettings.email` API response  
- **Verify**: Email is clickable with `mailto:` link
- **Fallback**: Shows translation value if API data unavailable

### 4. Dynamic Working Hours
- **Expected**: Shows today's working hours from `clinicSettings.workingHours`
- **Logic**: Automatically detects current day and shows relevant hours
- **Fallback**: Shows translation value if API data unavailable

### 5. Error Handling
- **Expected**: Graceful fallback to translation values if API fails
- **Verify**: No broken UI or missing contact information

## Implementation Details

### API Integration
- Uses same pattern as Footer component
- Fetches data via `api.getClinicSettings()`
- Stores in `clinicSettings` state with loading state

### Loading State
- Shows skeleton placeholders while loading
- Maintains visual consistency with rest of dashboard

### Fallback Strategy
- Falls back to translation values if no API data
- Ensures contact information is always available
- Maintains backward compatibility

## Testing Steps

1. **Load Dashboard Page**
   - Navigate to `/dashboard` as authenticated user
   - Observe loading state in contact section

2. **Verify API Data**
   - Check that contact info matches clinic settings from admin panel
   - Verify phone/email links work correctly
   - Confirm working hours show current day

3. **Test Fallbacks**
   - Simulate API failure
   - Verify fallback to translation values
   - Ensure no broken UI elements

4. **Multi-language Support**
   - Test in both English and Arabic
   - Verify translations work correctly
   - Check RTL layout for Arabic

## Success Criteria
- ✅ Contact information loads dynamically from API
- ✅ Loading states work properly
- ✅ Fallbacks function correctly
- ✅ Links are clickable and functional
- ✅ Multi-language support maintained
- ✅ No console errors or broken UI