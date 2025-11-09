# Internationalization (i18n) Implementation

## Overview

The SAAS Manager Admin Panel supports multiple languages with full internationalization (i18n) support. Currently, English and Arabic are supported with proper RTL (Right-to-Left) layout handling for Arabic.

## Configuration

### Nuxt Config

The i18n module is configured in `nuxt.config.ts`:

```typescript
i18n: {
  locales: [
    { code: 'en', name: 'English', file: 'en.json' },
    { code: 'ar', name: 'العربية', file: 'ar.json', dir: 'rtl' }
  ],
  defaultLocale: 'en',
  langDir: 'locales',
  strategy: 'no_prefix',
  detectBrowserLanguage: {
    useCookie: true,
    cookieKey: 'saas_admin_locale',
    redirectOn: 'root'
  }
}
```

### Translation Files

Translation files are located in the `locales/` directory:

- `locales/en.json` - English translations
- `locales/ar.json` - Arabic translations

## Features

### 1. Language Switcher

The language switcher is integrated into the UserMenu component. Users can switch between languages by:

1. Clicking on their user menu in the top right
2. Selecting "Change language" / "تغيير اللغة"
3. Choosing their preferred language

The selected language is persisted in a cookie (`saas_admin_locale`) and will be remembered across sessions.

### 2. RTL Support

Arabic language automatically enables RTL (Right-to-Left) layout:

- **Automatic Direction**: The `rtl-direction.client.ts` plugin automatically sets the `dir` attribute on the HTML element
- **CSS Adjustments**: Custom CSS in `assets/css/main.css` handles RTL-specific styling
- **Layout Mirroring**: UI elements are automatically mirrored for RTL languages

### 3. Translation Keys

All UI text uses translation keys organized by feature:

```typescript
// Example usage in components
{{ $t('dashboard.title') }}
{{ $t('tenants.createTenant') }}
{{ $t('common.save') }}
```

#### Key Categories

- **app**: Application-level text
- **login**: Login page
- **auth**: Authentication-related text
- **nav**: Navigation menu items
- **dashboard**: Dashboard page
- **tenants**: Tenant management
- **analytics**: Analytics page
- **auditLogs**: Audit logs page
- **notifications**: Notification messages
- **common**: Common UI elements (buttons, labels, etc.)
- **errors**: Error messages
- **language**: Language switcher

## Usage in Components

### Template Usage

```vue
<template>
  <h1>{{ $t('dashboard.title') }}</h1>
  <p>{{ $t('dashboard.systemMetrics') }}</p>
</template>
```

### Script Usage

```vue
<script setup lang="ts">
const { t } = useI18n()

const message = computed(() => t('tenants.success.created'))
</script>
```

### With Parameters

```vue
<template>
  <p>{{ $t('tenants.form.adminEmailDescription', { email: adminEmail }) }}</p>
</template>
```

### Pluralization

```vue
<template>
  <p>{{ $t('notifications.time.minutesAgo', { count: 5 }) }}</p>
</template>
```

In the translation file:
```json
{
  "notifications": {
    "time": {
      "minutesAgo": "{count} minute ago | {count} minutes ago"
    }
  }
}
```

## Adding New Translations

### 1. Add to English File

Edit `locales/en.json`:

```json
{
  "myFeature": {
    "title": "My Feature",
    "description": "This is my feature"
  }
}
```

### 2. Add to Arabic File

Edit `locales/ar.json`:

```json
{
  "myFeature": {
    "title": "ميزتي",
    "description": "هذه هي ميزتي"
  }
}
```

### 3. Use in Components

```vue
<template>
  <h1>{{ $t('myFeature.title') }}</h1>
  <p>{{ $t('myFeature.description') }}</p>
</template>
```

## RTL Styling Guidelines

### Automatic Handling

Most layout adjustments are handled automatically by the browser when `dir="rtl"` is set. However, some custom CSS may be needed for specific cases.

### Custom RTL Styles

Add RTL-specific styles in `assets/css/main.css`:

```css
/* RTL-specific adjustments */
[dir="rtl"] .my-component {
  /* Your RTL styles */
}
```

### Common RTL Patterns

```css
/* Spacing */
[dir="rtl"] .space-x-2 > * + * {
  margin-right: 0.5rem;
  margin-left: 0;
}

/* Text alignment */
[dir="rtl"] .text-left {
  text-align: right;
}

/* Padding */
[dir="rtl"] .pl-4 {
  padding-left: 0;
  padding-right: 1rem;
}
```

## Testing

### Manual Testing

1. Start the development server:
   ```bash
   npm run dev
   ```

2. Open the application in your browser

3. Switch between English and Arabic using the language switcher

4. Verify:
   - All text is translated correctly
   - RTL layout works properly for Arabic
   - Language preference is persisted across page reloads

### Browser Language Detection

The application automatically detects the browser's preferred language on first visit. To test:

1. Clear cookies
2. Change your browser's language preference
3. Visit the application
4. Verify it loads in the correct language

## Best Practices

### 1. Always Use Translation Keys

❌ **Don't:**
```vue
<button>Save</button>
```

✅ **Do:**
```vue
<button>{{ $t('common.save') }}</button>
```

### 2. Organize Keys Logically

Group related translations together:

```json
{
  "tenants": {
    "form": {
      "slug": "Tenant Slug",
      "name": "Tenant Name"
    },
    "success": {
      "created": "Tenant Created",
      "updated": "Tenant Updated"
    }
  }
}
```

### 3. Use Parameters for Dynamic Content

```vue
{{ $t('notifications.tenantCreatedMessage', { name: tenantName }) }}
```

### 4. Provide Context in Key Names

Use descriptive key names that provide context:

```json
{
  "tenants": {
    "form": {
      "slugHelp": "Unique identifier for the tenant",
      "slugPlaceholder": "e.g., dental-clinic-nyc"
    }
  }
}
```

### 5. Test Both Languages

Always test your changes in both English and Arabic to ensure:
- Translations are accurate
- Layout works in both LTR and RTL
- Text doesn't overflow or break the layout

## Troubleshooting

### Translation Not Showing

1. Check if the key exists in both `en.json` and `ar.json`
2. Verify the key path is correct
3. Check browser console for i18n warnings

### RTL Layout Issues

1. Verify the `dir` attribute is set on the HTML element
2. Check if custom CSS is overriding RTL styles
3. Use browser DevTools to inspect element direction

### Language Not Persisting

1. Check if cookies are enabled in the browser
2. Verify the `saas_admin_locale` cookie is being set
3. Check browser console for cookie-related errors

## Future Enhancements

Potential improvements for the i18n system:

1. **Additional Languages**: Add support for more languages (French, Spanish, etc.)
2. **Date/Time Formatting**: Locale-specific date and time formatting
3. **Number Formatting**: Locale-specific number formatting
4. **Currency Formatting**: Multi-currency support with proper formatting
5. **Translation Management**: Integration with translation management platforms
6. **Lazy Loading**: Load translations on-demand for better performance
7. **Translation Validation**: Automated checks for missing translations
