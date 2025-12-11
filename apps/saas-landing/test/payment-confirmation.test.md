# Payment Confirmation Page - Manual Test Guide

## Overview
This guide provides manual testing steps for the Payment Confirmation page that handles PayPal redirect callbacks after subscription approval.

## Test Environment Setup

### Prerequisites
- Backend API running at `http://localhost:8080`
- Frontend running at `http://localhost:3000`
- Valid PayPal sandbox credentials configured

## Test Scenarios

### 1. Successful Payment Confirmation

**Steps:**
1. Complete a signup flow that redirects to PayPal
2. Approve the subscription on PayPal sandbox
3. PayPal redirects back to: `/payment-confirmation?subscription_id=I-XXX&token=YYY`
4. Observe the payment confirmation page

**Expected Results:**
- ✅ Loading spinner displays with "Verifying Your Payment" message
- ✅ Progress bar animates during verification
- ✅ After verification, success message displays with green checkmark
- ✅ "Payment Confirmed!" heading appears
- ✅ Success message: "Your subscription has been activated successfully"
- ✅ Session token is stored in localStorage
- ✅ Page automatically redirects to admin panel after 2 seconds
- ✅ "Go to Admin Panel" button is visible and functional

**Verification:**
```javascript
// Check localStorage for session token
localStorage.getItem('sessionToken') // Should return a JWT token
```

### 2. Missing Query Parameters

**Steps:**
1. Navigate directly to `/payment-confirmation` without query parameters
2. Observe the error state

**Expected Results:**
- ✅ No loading spinner appears
- ✅ Error message displays immediately
- ✅ Red X icon appears
- ✅ Error text: "Missing payment confirmation parameters. Please contact support."
- ✅ "Retry Verification" button is visible
- ✅ "Contact Support" button is visible
- ✅ Subscription ID is shown (if available)

### 3. Invalid Subscription ID

**Steps:**
1. Navigate to `/payment-confirmation?subscription_id=INVALID&token=TEST`
2. Observe the error handling

**Expected Results:**
- ✅ Loading spinner displays initially
- ✅ API call is made to backend
- ✅ Error state displays after API response
- ✅ Error message from backend is shown
- ✅ "Retry Verification" button works
- ✅ "Contact Support" button opens email client with pre-filled details

### 4. Network Error

**Steps:**
1. Stop the backend API server
2. Navigate to `/payment-confirmation?subscription_id=I-TEST&token=TOKEN`
3. Observe error handling

**Expected Results:**
- ✅ Loading spinner displays initially
- ✅ After timeout, error state displays
- ✅ Error message: "Unable to connect to the server. Please check your internet connection and try again."
- ✅ Retry button is functional
- ✅ Contact support button works

### 5. Retry Functionality

**Steps:**
1. Trigger an error state (e.g., network error)
2. Click "Retry Verification" button
3. Observe the retry behavior

**Expected Results:**
- ✅ Loading state displays again
- ✅ API call is retried
- ✅ If backend is available, verification proceeds
- ✅ Retry count increments (internal state)

### 6. Contact Support

**Steps:**
1. Trigger an error state
2. Click "Contact Support" button
3. Observe the behavior

**Expected Results:**
- ✅ Email client opens
- ✅ Email is pre-addressed to: support@cliniqax.com
- ✅ Subject line: "Payment Confirmation Issue"
- ✅ Body includes subscription ID

### 7. Responsive Design

**Test on different screen sizes:**

**Mobile (375px):**
- ✅ Card is properly sized with margins
- ✅ Text is readable
- ✅ Buttons are full-width
- ✅ Icons are appropriately sized
- ✅ No horizontal scrolling

**Tablet (768px):**
- ✅ Card is centered
- ✅ Max-width constraint applied
- ✅ Layout remains clean

**Desktop (1920px):**
- ✅ Card is centered
- ✅ Max-width constraint applied
- ✅ Proper spacing maintained

### 8. Dark Mode

**Steps:**
1. Enable dark mode in browser/OS
2. Navigate to payment confirmation page
3. Test all states (loading, success, error)

**Expected Results:**
- ✅ Background color adapts to dark theme
- ✅ Card background is appropriate for dark mode
- ✅ Text colors have sufficient contrast
- ✅ Icons are visible
- ✅ Buttons maintain proper styling

### 9. Accessibility

**Keyboard Navigation:**
- ✅ Tab through interactive elements
- ✅ Focus indicators are visible
- ✅ Enter key activates buttons

**Screen Reader:**
- ✅ Loading state is announced
- ✅ Success message is announced
- ✅ Error messages are announced
- ✅ Button labels are descriptive

### 10. Browser Compatibility

Test on:
- ✅ Chrome (latest)
- ✅ Firefox (latest)
- ✅ Safari (latest)
- ✅ Edge (latest)

## API Integration Tests

### Verify API Response Handling

**Success Response:**
```json
{
  "success": true,
  "sessionToken": "eyJhbGciOiJSUzI1NiJ9...",
  "redirectUrl": "http://localhost:3001/tenant-slug/dashboard"
}
```

**Error Response:**
```json
{
  "success": false,
  "error": "Subscription not found. Please try signing up again."
}
```

## Performance Tests

### Loading Time
- ✅ Initial page load < 1 second
- ✅ API call completes within 3 seconds
- ✅ Redirect happens 2 seconds after success

### Memory Leaks
- ✅ No memory leaks after multiple retries
- ✅ Event listeners are cleaned up
- ✅ No console errors

## Security Tests

### Session Token Storage
- ✅ Token is stored in localStorage (not sessionStorage)
- ✅ Token is only stored on successful verification
- ✅ Token is not exposed in URL or console logs

### URL Parameter Validation
- ✅ subscription_id is validated
- ✅ token is validated
- ✅ No XSS vulnerabilities in error messages

## Edge Cases

### 1. Multiple Rapid Retries
**Steps:**
1. Trigger error state
2. Click retry button multiple times rapidly

**Expected:**
- ✅ Only one API call is made at a time
- ✅ No race conditions
- ✅ UI remains stable

### 2. Browser Back Button
**Steps:**
1. Complete successful verification
2. Click browser back button

**Expected:**
- ✅ Page shows success state (not re-verifying)
- ✅ No duplicate API calls

### 3. Page Refresh During Verification
**Steps:**
1. Start verification
2. Refresh page during loading

**Expected:**
- ✅ Verification restarts
- ✅ No errors occur
- ✅ State is properly reset

## Test Results Template

| Test Case | Status | Notes |
|-----------|--------|-------|
| Successful Payment | ⬜ | |
| Missing Parameters | ⬜ | |
| Invalid Subscription | ⬜ | |
| Network Error | ⬜ | |
| Retry Functionality | ⬜ | |
| Contact Support | ⬜ | |
| Mobile Responsive | ⬜ | |
| Dark Mode | ⬜ | |
| Accessibility | ⬜ | |
| Browser Compatibility | ⬜ | |

## Notes
- All tests should be performed in both light and dark modes
- Test with real PayPal sandbox credentials for end-to-end validation
- Monitor browser console for any errors or warnings
- Check network tab for API call details
