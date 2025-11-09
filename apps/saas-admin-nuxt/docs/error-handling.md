# Error Handling and Loading States

This document describes the comprehensive error handling and loading state system implemented in the SAAS Manager Admin Panel.

## Overview

The error handling system provides:
- **Error Boundary**: Catches unexpected errors and provides recovery options
- **API Error Handling**: Centralized error handling with user-friendly messages
- **Connection Monitoring**: Detects network issues and provides retry functionality
- **Loading Skeletons**: Visual feedback during data fetching
- **Form Validation**: Inline error messages for form fields
- **Toast Notifications**: Non-intrusive error and success messages

## Components

### 1. ErrorBoundary

Wraps the entire application to catch unexpected errors.

**Location**: `components/ErrorBoundary.vue`

**Features**:
- Catches unhandled errors and promise rejections
- Displays user-friendly error message
- Provides reload and navigation options
- Shows error details in development mode
- Automatically resets on route change

**Usage**:
```vue
<ErrorBoundary>
  <YourApp />
</ErrorBoundary>
```

### 2. LoadingSkeleton

Provides visual feedback during data loading.

**Location**: `components/LoadingSkeleton.vue`

**Types**:
- `card`: For card-based layouts
- `table`: For table views
- `metric`: For metric cards
- `form`: For form fields
- `chart`: For chart components
- `text`: For text content

**Usage**:
```vue
<LoadingSkeleton type="table" :rows="5" />
<LoadingSkeleton type="metric" />
<LoadingSkeleton type="card" />
```

### 3. ConnectionLost

Displays a banner when network connection is lost.

**Location**: `components/ConnectionLost.vue`

**Features**:
- Automatically detects connection loss
- Shows reconnecting state
- Provides manual retry button
- Auto-hides when connection restored

**Usage**:
```vue
<ConnectionLost 
  :visible="connectionLostVisible" 
  :on-retry="retryConnection"
/>
```

### 4. FormError

Displays inline validation errors for form fields.

**Location**: `components/FormError.vue`

**Features**:
- Animated entrance/exit
- Icon with error message
- Accessible with ARIA attributes

**Usage**:
```vue
<UInput v-model="email" />
<FormError :error="errors.email" />
```

## Composables

### 1. useApiErrorHandler

Centralized API error handling with retry logic.

**Location**: `composables/useApiErrorHandler.ts`

**Methods**:

#### `handleError(error, customMessage?)`
Displays user-friendly error notification.

```typescript
const { handleError } = useApiErrorHandler()

try {
  await api.createTenant(data)
} catch (error) {
  handleError(error, 'Failed to create tenant')
}
```

#### `retry(fn, options?)`
Retries failed API calls with exponential backoff.

```typescript
const { retry } = useApiErrorHandler()

const data = await retry(
  () => api.getTenants(),
  { maxRetries: 3, retryDelay: 1000 }
)
```

#### `getErrorMessage(error)`
Converts API errors to user-friendly messages.

```typescript
const { getErrorMessage } = useApiErrorHandler()
const message = getErrorMessage(error)
```

#### `extractValidationErrors(error)`
Extracts field-specific validation errors.

```typescript
const { extractValidationErrors } = useApiErrorHandler()
const fieldErrors = extractValidationErrors(error)
// { email: 'Invalid email format', slug: 'Slug already exists' }
```

### 2. useConnectionStatus

Monitors network connection status.

**Location**: `composables/useConnectionStatus.ts`

**Properties**:
- `isOnline`: Current connection status
- `connectionLostVisible`: Whether to show connection lost banner
- `lastOnlineTime`: Last time connection was active

**Methods**:
- `checkConnection()`: Manually check connection
- `retryConnection()`: Attempt to restore connection
- `hideConnectionLost()`: Hide connection lost banner

**Usage**:
```typescript
const { isOnline, connectionLostVisible, retryConnection } = useConnectionStatus()

if (!isOnline.value) {
  // Handle offline state
}
```

### 3. useNotifications

Toast notification system (already implemented).

**Location**: `composables/useNotifications.ts`

**Methods**:
- `success(title, message)`: Show success notification
- `error(title, message)`: Show error notification
- `warning(title, message)`: Show warning notification
- `info(title, message)`: Show info notification

## Error Messages

All error messages are internationalized and available in English and Arabic.

**Translation Keys** (in `locales/en.json` and `locales/ar.json`):

```json
{
  "errors": {
    "unexpectedError": "Something Went Wrong",
    "networkError": "Network connection error",
    "unauthorized": "Your session has expired",
    "forbidden": "You don't have permission",
    "notFound": "Resource not found",
    "serverError": "Server error occurred",
    "connectionLost": "Connection Lost",
    "retry": "Retry"
  }
}
```

## HTTP Status Code Handling

The system automatically handles common HTTP status codes:

- **400 Bad Request**: Shows validation error message
- **401 Unauthorized**: Logs out user and redirects to login
- **403 Forbidden**: Shows permission denied message
- **404 Not Found**: Shows resource not found message
- **409 Conflict**: Shows conflict message (e.g., duplicate slug)
- **422 Unprocessable Entity**: Shows validation errors
- **429 Too Many Requests**: Shows rate limit message
- **500 Server Error**: Shows server error message
- **502 Bad Gateway**: Shows service unavailable message
- **503 Service Unavailable**: Shows service unavailable message
- **504 Gateway Timeout**: Shows timeout message

## Retry Logic

The retry system uses exponential backoff for transient errors:

**Retryable Status Codes**: 408, 429, 500, 502, 503, 504, and network errors

**Default Configuration**:
- Max retries: 3
- Initial delay: 1000ms
- Backoff multiplier: 2x (1s, 2s, 4s)

**Example**:
```typescript
const { retry } = useApiErrorHandler()

const data = await retry(
  () => api.getTenants(),
  { 
    maxRetries: 3, 
    retryDelay: 1000,
    retryOn: [500, 502, 503] // Custom retry conditions
  }
)
```

## Best Practices

### 1. Always Show Loading States

```vue
<template>
  <LoadingSkeleton v-if="loading" type="table" />
  <TenantTable v-else :tenants="tenants" />
</template>
```

### 2. Handle Errors Gracefully

```typescript
try {
  await api.createTenant(data)
  success('Success', 'Tenant created successfully')
} catch (error) {
  handleError(error, 'Failed to create tenant')
}
```

### 3. Provide Retry Options

```vue
<UAlert v-if="error" color="red">
  <template #actions>
    <UButton @click="retry">Retry</UButton>
  </template>
</UAlert>
```

### 4. Use Inline Validation

```vue
<UInput v-model="email" />
<FormError :error="errors.email" />
```

### 5. Monitor Connection Status

The connection status is automatically monitored. The ConnectionLost component will appear when the connection is lost.

## Testing Error Handling

### Simulate Network Error
```typescript
// In browser console
window.dispatchEvent(new Event('offline'))
```

### Simulate API Error
```typescript
// Mock API response
api.getTenants = () => Promise.reject({ status: 500 })
```

### Test Retry Logic
```typescript
let attempts = 0
const flaky = () => {
  attempts++
  if (attempts < 3) throw new Error('Network error')
  return { data: 'success' }
}

await retry(flaky) // Will succeed on 3rd attempt
```

## Accessibility

All error components follow accessibility best practices:

- **ARIA attributes**: `role="alert"`, `aria-live="polite"`
- **Keyboard navigation**: All interactive elements are keyboard accessible
- **Screen reader support**: Error messages are announced
- **Color contrast**: Meets WCAG AA standards
- **Focus management**: Focus is managed appropriately

## Performance

- **Debouncing**: Search inputs are debounced (300ms)
- **Lazy loading**: Components are loaded on demand
- **Optimistic updates**: UI updates before API confirmation
- **Caching**: API responses are cached appropriately
- **Skeleton screens**: Prevent layout shift during loading
