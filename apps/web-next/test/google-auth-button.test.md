# GoogleAuthButton Component - Manual Test Guide

## Overview
This document provides manual testing instructions for the GoogleAuthButton component.

## Component Location
`apps/web-next/components/GoogleAuthButton.tsx`

## Test Prerequisites
1. Backend API must be running with Google OAuth configured
2. Google OAuth credentials must be set in environment variables
3. Valid tenant context must be available

## Test Cases

### Test 1: Component Renders Correctly (Sign In Mode)
**Steps:**
1. Navigate to the login page
2. Verify the "Continue with Google" button is visible
3. Verify the Google logo is displayed
4. Verify the button text matches the locale (English: "Continue with Google", Arabic: "المتابعة مع Google")

**Expected Result:**
- Button renders with proper styling
- Google logo is visible and colored correctly
- Text is properly translated

### Test 2: Component Renders Correctly (Sign Up Mode)
**Steps:**
1. Navigate to the signup page
2. Verify the "Sign up with Google" button is visible
3. Verify the Google logo is displayed
4. Verify the button text matches the locale (English: "Sign up with Google", Arabic: "التسجيل مع Google")

**Expected Result:**
- Button renders with proper styling
- Google logo is visible and colored correctly
- Text is properly translated

### Test 3: Loading State
**Steps:**
1. Click the Google auth button
2. Observe the button state during redirect

**Expected Result:**
- Button shows loading spinner
- Button text changes to "Redirecting to Google..." (or Arabic equivalent)
- Button is disabled during loading
- User is redirected to Google OAuth consent screen

### Test 4: Tenant Context Validation
**Steps:**
1. Clear tenant context (e.g., clear cookies)
2. Click the Google auth button
3. Observe error handling

**Expected Result:**
- Error is caught and handled gracefully
- Error message: "Tenant context not found. Please refresh the page and try again."
- onError callback is called if provided

### Test 5: Hover State
**Steps:**
1. Hover over the Google auth button
2. Observe visual feedback

**Expected Result:**
- Background color changes slightly
- Button text shifts slightly to the right
- Cursor changes to pointer

### Test 6: Disabled State
**Steps:**
1. Programmatically disable the button
2. Try to click it

**Expected Result:**
- Button appears dimmed (opacity 50%)
- Cursor shows not-allowed
- Click has no effect

### Test 7: Accessibility
**Steps:**
1. Use keyboard navigation (Tab key)
2. Focus on the Google auth button
3. Press Enter or Space

**Expected Result:**
- Button receives focus with visible outline
- Button can be activated with keyboard
- Screen reader announces proper aria-label

### Test 8: RTL Support (Arabic)
**Steps:**
1. Switch to Arabic locale
2. Navigate to login/signup page
3. Observe button layout

**Expected Result:**
- Button text is in Arabic
- Layout is properly mirrored for RTL
- Google logo remains in correct position

### Test 9: Dark Mode
**Steps:**
1. Enable dark mode
2. Observe button styling

**Expected Result:**
- Button background adapts to dark theme
- Text color is readable
- Border color is appropriate for dark mode

### Test 10: Error Handling
**Steps:**
1. Mock a network error or invalid tenant
2. Click the Google auth button
3. Observe error handling

**Expected Result:**
- Error is caught
- onError callback is called with error object
- User sees appropriate error message
- Button returns to normal state (not loading)

## Integration Testing

### Test 11: Full OAuth Flow
**Steps:**
1. Click "Continue with Google" button
2. Complete Google OAuth consent
3. Verify redirect back to application
4. Verify JWT token is received
5. Verify user is authenticated

**Expected Result:**
- User is redirected to Google
- After consent, user is redirected back
- JWT token is stored
- User is logged in and redirected to dashboard

### Test 12: Multi-Tenant Isolation
**Steps:**
1. Test with different tenant contexts
2. Verify tenant slug is included in OAuth request
3. Verify separate patient records are created per tenant

**Expected Result:**
- Tenant context is preserved throughout flow
- State parameter includes tenant information
- Patient records are properly scoped to tenant

## Browser Compatibility
Test the component in:
- [ ] Chrome (latest)
- [ ] Firefox (latest)
- [ ] Safari (latest)
- [ ] Edge (latest)
- [ ] Mobile Safari (iOS)
- [ ] Chrome Mobile (Android)

## Performance
- [ ] Button responds immediately to click
- [ ] No layout shift when button loads
- [ ] Smooth transitions and animations

## Security
- [ ] Tenant context is validated before redirect
- [ ] No sensitive data exposed in console
- [ ] HTTPS is used in production
- [ ] State parameter is generated securely (backend)

## Notes
- The component relies on backend OAuth endpoints being properly configured
- Google OAuth credentials must be valid
- Tenant context must be available from cookies or hostname
- Component uses next-intl for translations

## Related Files
- Component: `apps/web-next/components/GoogleAuthButton.tsx`
- Translations: `apps/web-next/messages/en.json`, `apps/web-next/messages/ar.json`
- Tenant Utils: `apps/web-next/lib/tenant.ts`
- API Utils: `apps/web-next/lib/api.ts`
