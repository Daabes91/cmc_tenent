# SignupForm Component Tests

## Test Suite: SignupForm Component

### Test 1: Form Validation - Clinic Name

**Test Case**: Validate clinic name field requirements

**Steps**:
1. Open the signup form
2. Leave clinic name field empty and try to submit
3. Enter "A" (1 character) in clinic name
4. Enter a valid clinic name (e.g., "Happy Dental Clinic")

**Expected Results**:
- Empty field shows error: "Clinic name must be at least 2 characters"
- Single character shows error: "Clinic name must be at least 2 characters"
- Valid name (2+ characters) passes validation
- Field accepts up to 100 characters

**Status**: ✅ Pass / ❌ Fail

---

### Test 2: Form Validation - Subdomain Format

**Test Case**: Validate subdomain format and character restrictions

**Steps**:
1. Enter "AB" (uppercase) in subdomain field
2. Enter "test_subdomain" (with underscore)
3. Enter "-test" (starting with hyphen)
4. Enter "test-" (ending with hyphen)
5. Enter "valid-subdomain-123" (valid format)

**Expected Results**:
- Uppercase letters are automatically converted to lowercase
- Underscore shows error: "Subdomain must contain only lowercase letters, numbers, and hyphens"
- Starting with hyphen shows error
- Ending with hyphen shows error
- Valid format passes validation

**Status**: ✅ Pass / ❌ Fail

---

### Test 3: Subdomain Availability Check - Debouncing

**Test Case**: Verify debounced subdomain availability check

**Steps**:
1. Type "test" in subdomain field
2. Wait 300ms
3. Type "ing" to make "testing"
4. Wait 600ms and observe the check indicator

**Expected Results**:
- No API call is made while typing rapidly
- API call is made 500ms after user stops typing
- Loading spinner appears during check
- Check mark (✓) appears if available
- X mark appears if unavailable

**Status**: ✅ Pass / ❌ Fail

---

### Test 4: Subdomain Availability Check - Available

**Test Case**: Test subdomain availability check with available subdomain

**Steps**:
1. Enter a unique subdomain (e.g., "unique-clinic-12345")
2. Wait for the availability check to complete

**Expected Results**:
- Loading spinner appears briefly
- Green check mark (✓) appears
- No error message is shown
- Submit button remains enabled

**Status**: ✅ Pass / ❌ Fail

---

### Test 5: Subdomain Availability Check - Unavailable

**Test Case**: Test subdomain availability check with taken subdomain

**Steps**:
1. Enter a subdomain that already exists (e.g., "demo")
2. Wait for the availability check to complete

**Expected Results**:
- Loading spinner appears briefly
- Red X mark appears
- Error message shows: "This subdomain is already taken"
- Submit button is disabled

**Status**: ✅ Pass / ❌ Fail

---

### Test 6: Form Validation - Email Format

**Test Case**: Validate email field format

**Steps**:
1. Enter "notanemail" (no @ symbol)
2. Enter "test@" (incomplete)
3. Enter "test@domain" (no TLD)
4. Enter "test@domain.com" (valid)

**Expected Results**:
- Invalid formats show error: "Please enter a valid email address"
- Valid email format passes validation

**Status**: ✅ Pass / ❌ Fail

---

### Test 7: Form Validation - Password Strength

**Test Case**: Validate password strength requirements

**Steps**:
1. Enter "short" (less than 8 characters)
2. Enter "lowercase123!" (no uppercase)
3. Enter "UPPERCASE123!" (no lowercase)
4. Enter "NoNumbers!" (no numbers)
5. Enter "NoSpecial123" (no special characters)
6. Enter "Valid123!" (meets all requirements)

**Expected Results**:
- Each invalid password shows appropriate error message
- Valid password passes all checks
- Error messages are specific to missing requirement

**Status**: ✅ Pass / ❌ Fail

---

### Test 8: Form Validation - Phone Number

**Test Case**: Validate phone number format

**Steps**:
1. Enter "123" (too short)
2. Enter "abcdefghij" (letters)
3. Enter "+1234567890" (valid with country code)
4. Enter "(555) 123-4567" (valid with formatting)

**Expected Results**:
- Too short shows error: "Phone number must be at least 10 characters"
- Letters show error: "Please enter a valid phone number"
- Valid formats pass validation
- Accepts +, spaces, hyphens, and parentheses

**Status**: ✅ Pass / ❌ Fail

---

### Test 9: Successful Submission

**Test Case**: Test successful form submission and PayPal redirect

**Steps**:
1. Fill all fields with valid data:
   - Clinic Name: "Test Clinic"
   - Subdomain: "test-clinic-unique"
   - Owner Name: "Dr. Test"
   - Email: "test@clinic.com"
   - Password: "Test123!"
   - Phone: "+1234567890"
2. Click "Continue to Payment" button
3. Mock API response with success and approval URL

**Expected Results**:
- Submit button shows loading state: "Creating your clinic..."
- API call is made to `/api/public/signup` with correct data
- On success, browser redirects to PayPal approval URL
- No error messages are shown

**Status**: ✅ Pass / ❌ Fail

---

### Test 10: Error Handling - Subdomain Conflict

**Test Case**: Handle subdomain already exists error from API

**Steps**:
1. Fill form with valid data but use existing subdomain
2. Submit the form
3. Mock API response with error: "Subdomain already exists"

**Expected Results**:
- Error alert appears at top of form
- Error message displays: "Subdomain already exists"
- Form remains filled with user data
- User can correct and resubmit

**Status**: ✅ Pass / ❌ Fail

---

### Test 11: Error Handling - PayPal API Failure

**Test Case**: Handle PayPal service unavailable error

**Steps**:
1. Fill form with valid data
2. Submit the form
3. Mock API response with error: "Payment service temporarily unavailable"

**Expected Results**:
- Error alert appears with user-friendly message
- Submit button returns to normal state
- Form data is preserved
- User can retry submission

**Status**: ✅ Pass / ❌ Fail

---

### Test 12: Error Handling - Network Error

**Test Case**: Handle network connection failure

**Steps**:
1. Fill form with valid data
2. Disconnect network or mock network error
3. Submit the form

**Expected Results**:
- Error message shows: "Unable to connect to the server. Please check your internet connection and try again."
- Submit button returns to normal state
- Form remains interactive

**Status**: ✅ Pass / ❌ Fail

---

### Test 13: Loading States

**Test Case**: Verify all loading states during form interaction

**Steps**:
1. Type in subdomain field and observe loading indicator
2. Submit form and observe button loading state
3. Check that form fields are disabled during submission

**Expected Results**:
- Subdomain check shows spinner while checking
- Submit button shows "Creating your clinic..." with spinner
- All form fields are disabled during submission
- Submit button is disabled during submission

**Status**: ✅ Pass / ❌ Fail

---

### Test 14: Form Field Descriptions

**Test Case**: Verify helpful descriptions are shown for each field

**Steps**:
1. Review each form field for description text

**Expected Results**:
- Clinic Name: "The official name of your clinic"
- Subdomain: Shows preview URL format
- Owner Name: "Full name of the clinic owner/administrator"
- Email: "We'll use this for your account login"
- Password: Shows strength requirements
- Phone: "Include country code (e.g., +1 for US)"

**Status**: ✅ Pass / ❌ Fail

---

### Test 15: Accessibility

**Test Case**: Verify form accessibility features

**Steps**:
1. Navigate form using keyboard only (Tab key)
2. Use screen reader to read form labels and errors
3. Check ARIA attributes on form elements

**Expected Results**:
- All fields are keyboard accessible
- Tab order is logical
- Labels are properly associated with inputs
- Error messages are announced by screen readers
- Required fields are marked with *

**Status**: ✅ Pass / ❌ Fail

---

### Test 16: Responsive Design

**Test Case**: Verify form works on different screen sizes

**Steps**:
1. View form on mobile (375px width)
2. View form on tablet (768px width)
3. View form on desktop (1280px width)

**Expected Results**:
- Form is readable and usable on all screen sizes
- Buttons stack vertically on mobile
- Input fields are appropriately sized
- No horizontal scrolling required

**Status**: ✅ Pass / ❌ Fail

---

## Test Execution Summary

**Total Tests**: 16
**Passed**: ___ / 16
**Failed**: ___ / 16
**Blocked**: ___ / 16

**Test Date**: ___________
**Tester**: ___________
**Environment**: ___________

## Notes

- All tests should be run with both valid and invalid data
- API mocking should be used for consistent test results
- Tests should be automated where possible using testing frameworks
- Manual testing should verify visual appearance and user experience

## Known Issues

_Document any known issues or limitations here_

## Future Test Cases

- Test form with very long input values (boundary testing)
- Test form behavior with slow network connections
- Test concurrent subdomain checks
- Test form state persistence on page refresh
- Test browser autofill compatibility
