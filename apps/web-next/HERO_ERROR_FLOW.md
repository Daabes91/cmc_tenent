# Hero Section Error Handling Flow

## Visual Flow Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                     Landing Page Loads                          │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│              Load Hero Media Settings from API                  │
└────────────────────────────┬────────────────────────────────────┘
                             │
                ┌────────────┴────────────┐
                │                         │
                ▼                         ▼
         ┌──────────┐              ┌──────────┐
         │ Success  │              │  Error   │
         └─────┬────┘              └─────┬────┘
               │                         │
               │                         ▼
               │              ┌─────────────────────┐
               │              │ Log Error to Console│
               │              │ Use Default Image   │
               │              └─────────────────────┘
               │
               ▼
┌─────────────────────────────────────────────────────────────────┐
│                  Check Media Type                               │
└────────────────────────────┬────────────────────────────────────┘
                             │
                ┌────────────┴────────────┐
                │                         │
                ▼                         ▼
         ┌──────────┐              ┌──────────┐
         │  Video   │              │  Image   │
         └─────┬────┘              └─────┬────┘
               │                         │
               ▼                         ▼
┌──────────────────────────┐  ┌──────────────────────────┐
│  Render YouTubeEmbed     │  │  Render Image Component  │
│  Component               │  │                          │
└─────┬────────────────────┘  └─────┬────────────────────┘
      │                             │
      │ ┌─────────────────┐         │ ┌─────────────────┐
      │ │ Start 10s Timer │         │ │ Image onError   │
      │ └────────┬────────┘         │ └────────┬────────┘
      │          │                  │          │
      │          ▼                  │          ▼
      │ ┌─────────────────┐         │ ┌─────────────────┐
      │ │ iframe onLoad   │         │ │ Use Default     │
      │ └────────┬────────┘         │ │ Image           │
      │          │                  │ └─────────────────┘
      │          ▼                  │
      │ ┌─────────────────┐         │
      │ │ Clear Timer     │         │
      │ │ Video Loaded ✓  │         │
      │ └─────────────────┘         │
      │                             │
      │ ┌─────────────────┐         │
      │ │ iframe onError  │         │
      │ │ OR Timeout      │         │
      │ └────────┬────────┘         │
      │          │                  │
      │          ▼                  │
      │ ┌─────────────────┐         │
      │ │ Call onError    │         │
      │ │ Return null     │         │
      │ └────────┬────────┘         │
      │          │                  │
      │          ▼                  │
      │ ┌─────────────────┐         │
      │ │ Parent Detects  │         │
      │ │ null, Shows     │         │
      │ │ Default Image   │         │
      │ └─────────────────┘         │
      │                             │
      ▼                             ▼
┌─────────────────────────────────────────────────────────────────┐
│              Error Boundary Wraps Everything                    │
│  (Catches any React errors and shows fallback image)            │
└─────────────────────────────────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                  Page Renders Successfully                      │
│              (Always shows some hero media)                     │
└─────────────────────────────────────────────────────────────────┘
```

## Error Handling Layers

### Layer 1: API Error Handling
- **Location:** `HomePageClient.tsx` - `loadHeroMedia()`
- **Catches:** API failures, network errors, authentication issues
- **Action:** Falls back to default image
- **Logging:** Detailed error with stack trace

### Layer 2: YouTube Video Error Handling
- **Location:** `YouTubeEmbed.tsx`
- **Catches:** 
  - Invalid video ID
  - Restricted/deleted videos
  - Network timeouts (>10s)
  - iframe load errors
- **Action:** Returns null, parent shows default image
- **Logging:** Error with video ID and reason

### Layer 3: Image Error Handling
- **Location:** `HomePageClient.tsx` - `handleImageError()`
- **Catches:** Invalid image URLs, broken links, network errors
- **Action:** Replaces with default image
- **Logging:** Error with attempted URL

### Layer 4: React Error Boundary
- **Location:** `HeroSectionErrorBoundary.tsx`
- **Catches:** Any React errors in hero section
- **Action:** Shows fallback UI with default image
- **Logging:** Error with component stack trace

## Fallback Cascade

```
Custom Video/Image
       ↓ (on error)
Default Image
       ↓ (on error)
Error Boundary Fallback
       ↓ (on error)
Page Still Renders
```

## Console Log Examples

### Success Case
```
Hero media settings loaded: {
  heroMediaType: "video",
  hasVideoId: true,
  hasImageUrl: false,
  timestamp: "2024-11-10T12:00:00.000Z"
}
YouTube video loaded successfully: dQw4w9WgXcQ
```

### Error Case (Video Timeout)
```
Hero media settings loaded: {
  heroMediaType: "video",
  hasVideoId: true,
  hasImageUrl: false,
  timestamp: "2024-11-10T12:00:00.000Z"
}
YouTube video load timeout: dQw4w9WgXcQ - Video may be unavailable or restricted
Failed to load YouTube video: dQw4w9WgXcQ {
  reason: "Load timeout - video may be unavailable",
  timestamp: "2024-11-10T12:00:10.000Z",
  videoId: "dQw4w9WgXcQ"
}
YouTube video failed to load, falling back to default image {
  videoId: "dQw4w9WgXcQ",
  timestamp: "2024-11-10T12:00:10.000Z"
}
```

### Error Case (API Failure)
```
Failed to load hero media settings: {
  error: "Network request failed",
  stack: "Error: Network request failed\n    at ...",
  timestamp: "2024-11-10T12:00:00.000Z"
}
```

### Error Case (Image Load Failure)
```
Hero image failed to load, falling back to default {
  attemptedUrl: "https://invalid.url/image.jpg",
  timestamp: "2024-11-10T12:00:05.000Z"
}
```

## Key Features

✅ **Multiple Error Handling Layers** - Redundant safety nets
✅ **Comprehensive Logging** - Every error logged with context
✅ **Graceful Degradation** - Always shows something to user
✅ **No User-Facing Errors** - Errors only in console
✅ **Timeout Detection** - Prevents indefinite loading
✅ **Error Boundary** - Prevents page crashes
✅ **Fallback Cascade** - Multiple fallback options

## Testing Scenarios

1. **Happy Path** - Video/image loads successfully
2. **Invalid Video ID** - Falls back to default image
3. **Slow Network** - Timeout triggers fallback
4. **Invalid Image URL** - Falls back to default image
5. **API Down** - Falls back to default image
6. **React Error** - Error boundary shows fallback
7. **No Custom Media** - Shows default image

All scenarios result in a functional page with hero media displayed.
