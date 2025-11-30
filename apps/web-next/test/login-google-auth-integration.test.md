# Login Page Google Auth Integration Test

## Test Overview
This document provides manual testing steps to verify the Google OAuth button integration on the login page.

## Requirements Validated
- **Requirement 1.1**: Display "Continue with Google" button on login page
- **Requirement 8.1**: Continue to support local email/password authentication
- **Requirement 8.5**: Present both methods with equal prominence

## Test Environment
- **Application**: web-next (Patient Portal)
- **Page**: `/login` or `/[locale]/login`
- **Component**: GoogleAuthButton integrated into LoginPage

## Pre-requisites
1. Application is running locally or deployed
2. Google OAuth is configured with valid credentials
3. Test tenant exists in the database

## Test Cases

### Test 1: Visual Layout and Positioning
**Objective**: Verify the Google Auth button is properly positioned and styled

**Steps**:
1. Navigate to the login page
2. Observe the page layout

**Expected Results**:
- ✅ Google "Continue with Google" button appears at the top of the form
- ✅ Button displays the Google logo (colored G icon)
- ✅ Button has proper styling (white background, border, hover effects)
- ✅ "or" divider appears below the Google button
- ✅ Email/password form appears below the divider
- ✅ Both authentication methods have equal visual prominence

### Test 2: Responsive Design
**Objective**: Verify the layout works on different screen sizes

**Steps**:
1. Open the login page on desktop (1920x1080)
2. Resize browser to tablet size (768px)
3. Resize browser to mobile size (375px)

**Expected Results**:
- ✅ Google button is full-width on all screen sizes
- ✅ Divider spans the full width appropriately
- ✅ Form elements stack properly on mobile
- ✅ No horizontal scrolling occurs
- ✅ Touch targets are appropriately sized on mobile (min 44x44px)

### Test 3: Google OAuth Flow Initiation
**Objective**: Verify clicking the Google button initiates OAuth flow

**Steps**:
1. Navigate to the login page
2. Click the "Continue with Google" button
3. Observe the behavior

**Expected Results**:
- ✅ Button shows loading state (spinner + "Redirecting to Google..." text)
- ✅ Button becomes disabled during loading
- ✅ Page redirects to Google OAuth consent screen
- ✅ Tenant context is preserved in the OAuth state parameter

### Test 4: Error Handling
**Objective**: Verify errors are displayed properly

**Steps**:
1. Simulate a network error (disconnect internet)
2. Click the "Continue with Google" button
3. Observe error handling

**Expected Results**:
- ✅ Error message appears in the error banner
- ✅ Error message is user-friendly
- ✅ Button returns to normal state after error
- ✅ User can retry the action

### Test 5: Local Authentication Still Works
**Objective**: Verify email/password login is unaffected

**Steps**:
1. Navigate to the login page
2. Enter valid email and password
3. Click "Sign In" button
4. Verify successful login

**Expected Results**:
- ✅ Email/password form is fully functional
- ✅ Login succeeds with valid credentials
- ✅ User is redirected to dashboard
- ✅ Session is created properly

### Test 6: Internationalization (i18n)
**Objective**: Verify translations work correctly

**Steps**:
1. Navigate to login page in English
2. Observe button text and divider
3. Switch to Arabic (if supported)
4. Observe button text and divider

**Expected Results**:
- ✅ English: Button shows "Continue with Google"
- ✅ English: Divider shows "or"
- ✅ Arabic: Button shows "المتابعة مع Google"
- ✅ Arabic: Divider shows "أو"
- ✅ RTL layout is properly applied in Arabic

### Test 7: Dark Mode Support
**Objective**: Verify the integration works in dark mode

**Steps**:
1. Enable dark mode in the application
2. Navigate to the login page
3. Observe the styling

**Expected Results**:
- ✅ Google button has appropriate dark mode styling
- ✅ Divider line is visible in dark mode
- ✅ Text colors have sufficient contrast
- ✅ All elements are readable

### Test 8: Accessibility
**Objective**: Verify accessibility compliance

**Steps**:
1. Navigate to login page
2. Use keyboard only (Tab, Enter)
3. Use screen reader (if available)

**Expected Results**:
- ✅ Google button is keyboard accessible (Tab to focus)
- ✅ Button can be activated with Enter/Space
- ✅ Button has proper aria-label
- ✅ Focus indicators are visible
- ✅ Screen reader announces button purpose correctly

### Test 9: Session Expired Flow
**Objective**: Verify Google auth works after session expiration

**Steps**:
1. Navigate to `/login?expired=true`
2. Observe the session expired message
3. Click "Continue with Google"

**Expected Results**:
- ✅ Session expired banner appears
- ✅ Google auth button is still functional
- ✅ OAuth flow completes successfully
- ✅ New session is created

### Test 10: Integration with Existing Error States
**Objective**: Verify Google auth errors integrate with existing error display

**Steps**:
1. Trigger a Google auth error
2. Observe error display
3. Try local login with wrong credentials
4. Observe error display

**Expected Results**:
- ✅ Google auth errors appear in the same error banner
- ✅ Local auth errors appear in the same error banner
- ✅ Only one error is shown at a time
- ✅ Error banner styling is consistent

## Browser Compatibility Testing

Test on the following browsers:
- [ ] Chrome (latest)
- [ ] Firefox (latest)
- [ ] Safari (latest)
- [ ] Edge (latest)
- [ ] Mobile Safari (iOS)
- [ ] Chrome Mobile (Android)

## Performance Considerations

- [ ] Page load time is not significantly impacted
- [ ] Google button renders without layout shift
- [ ] No console errors or warnings
- [ ] Network requests are optimized

## Security Checks

- [ ] Tenant slug is properly encoded in OAuth URL
- [ ] No sensitive data is exposed in client-side code
- [ ] HTTPS is enforced for OAuth redirects
- [ ] State parameter is cryptographically secure

## Test Results Summary

| Test Case | Status | Notes |
|-----------|--------|-------|
| Visual Layout | ⏳ Pending | |
| Responsive Design | ⏳ Pending | |
| OAuth Flow | ⏳ Pending | |
| Error Handling | ⏳ Pending | |
| Local Auth | ⏳ Pending | |
| i18n | ⏳ Pending | |
| Dark Mode | ⏳ Pending | |
| Accessibility | ⏳ Pending | |
| Session Expired | ⏳ Pending | |
| Error Integration | ⏳ Pending | |

## Known Issues
_Document any issues found during testing_

## Notes
_Additional observations or comments_

---

**Test Date**: _____________________
**Tester**: _____________________
**Environment**: _____________________
**Build/Version**: _____________________
