# SAAS Admin Panel Test Suite

This directory contains comprehensive tests for the SAAS Manager Admin Panel application.

## Test Structure

```
test/
├── setup.ts                      # Global test setup and mocks
├── composables/                  # Unit tests for composables
│   ├── useSaasAuth.test.ts
│   ├── useTenantManagement.test.ts
│   ├── useSystemMetrics.test.ts
│   └── useAnalytics.test.ts
├── components/                   # Component tests
│   ├── MetricCard.test.ts
│   ├── TenantTable.test.ts
│   ├── TenantForm.test.ts
│   └── BrandingEditor.test.ts
└── integration/                  # Integration tests
    ├── authentication.test.ts
    ├── tenant-management.test.ts
    └── route-guards.test.ts
```

## Running Tests

### Run all tests
```bash
npm test
```

### Run tests in watch mode
```bash
npm run test:watch
```

### Run tests with coverage
```bash
npm run test:coverage
```

### Run specific test file
```bash
npm test -- useSaasAuth.test.ts
```

### Run tests matching a pattern
```bash
npm test -- --grep "authentication"
```

## Test Coverage

The test suite covers:

### Unit Tests - Composables
- **useSaasAuth**: Login, logout, token management, session restoration
- **useTenantManagement**: CRUD operations, filtering, sorting, pagination
- **useSystemMetrics**: Metrics fetching, auto-refresh, error handling
- **useAnalytics**: Data fetching, date range calculations, PDF export

### Component Tests
- **MetricCard**: Rendering, value formatting, trend indicators
- **TenantTable**: List display, sorting, filtering, row interactions
- **TenantForm**: Validation, submission, slug uniqueness checking
- **BrandingEditor**: Color pickers, presets, logo upload, validation

### Integration Tests
- **Authentication Flow**: Complete login/logout cycle, session management
- **Tenant Management**: Create, update, delete operations with API
- **Route Guards**: Protected routes, authentication redirects

## Test Configuration

Tests are configured using Vitest with the following setup:

- **Environment**: happy-dom (lightweight DOM implementation)
- **Globals**: Enabled for describe, it, expect
- **Setup Files**: `test/setup.ts` for global mocks
- **Coverage**: v8 provider with text, JSON, and HTML reports

## Mocking Strategy

### Global Mocks
- `localStorage`: Mocked in setup.ts
- `$fetch`: Mocked per test file
- `useRuntimeConfig`: Mocked to provide test API URLs

### Component Mocks
- Nuxt UI components (UCard, UButton, UInput, etc.) are stubbed
- Router and route are mocked for navigation tests
- Toast notifications are mocked for user feedback tests

## Writing New Tests

### Unit Test Template
```typescript
import { describe, it, expect, vi, beforeEach } from 'vitest'

describe('MyComposable', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('should do something', () => {
    // Arrange
    const input = 'test'
    
    // Act
    const result = myFunction(input)
    
    // Assert
    expect(result).toBe('expected')
  })
})
```

### Component Test Template
```typescript
import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import MyComponent from '~/components/MyComponent.vue'

describe('MyComponent', () => {
  it('should render correctly', () => {
    const wrapper = mount(MyComponent, {
      props: {
        title: 'Test'
      },
      global: {
        stubs: {
          UCard: { template: '<div><slot /></div>' }
        }
      }
    })

    expect(wrapper.text()).toContain('Test')
  })
})
```

## Best Practices

1. **Isolation**: Each test should be independent and not rely on other tests
2. **Cleanup**: Use `beforeEach` to reset mocks and state
3. **Descriptive Names**: Test names should clearly describe what is being tested
4. **Arrange-Act-Assert**: Follow the AAA pattern for test structure
5. **Mock External Dependencies**: Mock API calls, localStorage, and other external dependencies
6. **Test Behavior, Not Implementation**: Focus on what the code does, not how it does it

## Troubleshooting

### Tests failing with module resolution errors
- Ensure all dependencies are installed: `npm install`
- Check that vitest.config.ts is properly configured
- Verify mock paths match actual file locations

### Tests timing out
- Check for unresolved promises
- Ensure async operations use `await`
- Verify mock functions are properly resolved

### Coverage not generating
- Run with `--coverage` flag
- Check that coverage provider is installed
- Verify exclude patterns in vitest.config.ts

## CI/CD Integration

Tests are designed to run in CI/CD pipelines:

```yaml
# Example GitHub Actions workflow
- name: Run tests
  run: npm test

- name: Generate coverage
  run: npm run test:coverage

- name: Upload coverage
  uses: codecov/codecov-action@v3
```

## Future Improvements

- [ ] Add E2E tests using Playwright
- [ ] Increase coverage to 90%+
- [ ] Add visual regression tests
- [ ] Add performance benchmarks
- [ ] Add accessibility tests
