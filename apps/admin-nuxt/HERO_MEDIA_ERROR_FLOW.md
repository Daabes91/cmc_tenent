# Hero Media Error Handling Flow Diagram

## Image Upload Flow

```
┌─────────────────────────────────────────────────────────────┐
│                    User Selects Image                        │
└─────────────────────┬───────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────┐
│              Validate File Type & Size                       │
└─────────────┬───────────────────────────┬───────────────────┘
              │                           │
         Valid│                           │Invalid
              ▼                           ▼
┌──────────────────────────┐   ┌──────────────────────────────┐
│  Show Loading Overlay    │   │  Show Error Toast            │
│  - Spinner Animation     │   │  - "Invalid file type" or    │
│  - "Uploading..." text   │   │    "File too large"          │
│  - Disable controls      │   │  - Red alert icon            │
└──────────┬───────────────┘   │  - 8 second timeout          │
           │                   └──────────────────────────────┘
           ▼                                  │
┌──────────────────────────┐                 │
│   Upload to Server       │                 │
└──────┬───────────┬───────┘                 │
       │           │                         │
Success│           │Error                    │
       ▼           ▼                         │
┌──────────────┐ ┌──────────────────────┐   │
│ Success Toast│ │  Error Toast         │   │
│ - Green icon │ │  - Red alert icon    │   │
│ - 3s timeout │ │  - Error message     │   │
└──────┬───────┘ │  - 8s timeout        │   │
       │         └──────────┬───────────┘   │
       │                    │                │
       ▼                    ▼                ▼
┌─────────────────────────────────────────────┐
│         Update Preview & Form State         │
│         User Can Retry Immediately          │
└─────────────────────────────────────────────┘
```

## YouTube URL Validation Flow

```
┌─────────────────────────────────────────────────────────────┐
│              User Enters YouTube URL                         │
└─────────────────────┬───────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────┐
│                  User Blurs Input                            │
└─────────────────────┬───────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────┐
│              Show Loading Spinner in Input                   │
│              Disable Input Field                             │
└─────────────────────┬───────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────┐
│           Validate URL with Regex Pattern                    │
└─────────────┬───────────────────────────┬───────────────────┘
              │                           │
         Valid│                           │Invalid
              ▼                           ▼
┌──────────────────────────┐   ┌──────────────────────────────┐
│  Success Toast           │   │  Error Toast                 │
│  - Green check icon      │   │  - Red alert icon            │
│  - "Valid YouTube URL"   │   │  - "Invalid YouTube URL"     │
│  - 3 second timeout      │   │  - 5 second timeout          │
└──────────┬───────────────┘   └──────────┬───────────────────┘
           │                              │
           ▼                              ▼
┌──────────────────────────┐   ┌──────────────────────────────┐
│  Show Green Check Icon   │   │  Show Red Alert Icon         │
│  in Input Field          │   │  in Input Field              │
│  Extract Video ID        │   │  Show Inline Error Message   │
│  Update Preview          │   │  Show URL Format Examples    │
└──────────────────────────┘   └──────────┬───────────────────┘
                                          │
                                          ▼
                              ┌──────────────────────────────┐
                              │  User Can Edit & Retry       │
                              │  Error Clears on Input       │
                              └──────────────────────────────┘
```

## Settings Save Flow

```
┌─────────────────────────────────────────────────────────────┐
│                User Clicks "Save Settings"                   │
└─────────────────────┬───────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────┐
│              Validate Hero Media Configuration               │
└─────────────┬───────────────────────────┬───────────────────┘
              │                           │
         Valid│                           │Invalid
              ▼                           ▼
┌──────────────────────────┐   ┌──────────────────────────────┐
│  Show Loading Toast      │   │  Show Warning Toast          │
│  - Blue spinner icon     │   │  - Amber warning icon        │
│  - "Saving settings..."  │   │  - "Invalid configuration"   │
│  - Manual dismiss        │   │  - Guidance message          │
│  - Disable save button   │   │  - 5 second timeout          │
└──────────┬───────────────┘   └──────────────────────────────┘
           │                                  │
           ▼                                  │
┌──────────────────────────┐                 │
│   Send to API Server     │                 │
└──────┬───────────┬───────┘                 │
       │           │                         │
Success│           │Error                    │
       ▼           ▼                         │
┌──────────────┐ ┌──────────────────────┐   │
│ Remove       │ │  Remove Loading      │   │
│ Loading Toast│ │  Toast               │   │
└──────┬───────┘ └──────────┬───────────┘   │
       │                    │                │
       ▼                    ▼                │
┌──────────────┐ ┌──────────────────────┐   │
│ Success Toast│ │  Error Toast         │   │
│ - Green icon │ │  - Red alert icon    │   │
│ - Context msg│ │  - Error details     │   │
│ - 5s timeout │ │  - 8s timeout        │   │
└──────┬───────┘ └──────────┬───────────┘   │
       │                    │                │
       ▼                    ▼                ▼
┌─────────────────────────────────────────────┐
│         Refresh Data / Preserve Form        │
│         User Can Retry if Error             │
└─────────────────────────────────────────────┘
```

## Clear Media Flow

```
┌─────────────────────────────────────────────────────────────┐
│              User Clicks "Clear Media"                       │
└─────────────────────┬───────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────┐
│              Clear All Media Configuration                   │
│              - Reset image URL                               │
│              - Reset video ID                                │
│              - Clear YouTube URL input                       │
│              - Clear validation errors                       │
└─────────────────────┬───────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────┐
│              Show Info Toast                                 │
│              - Blue info icon                                │
│              - "Media Cleared"                               │
│              - Confirmation message                          │
│              - 3 second timeout                              │
└─────────────────────┬───────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────┐
│              Update Preview to Default                       │
│              Form Ready for New Configuration                │
└─────────────────────────────────────────────────────────────┘
```

## Error Recovery Patterns

### Pattern 1: Immediate Retry (Upload Errors)
```
Error → Toast Notification → State Reset → User Can Retry Immediately
```

### Pattern 2: Edit and Retry (Validation Errors)
```
Error → Inline Error + Toast → User Edits → Auto-Revalidate on Blur
```

### Pattern 3: Preserve and Retry (Save Errors)
```
Error → Toast Notification → Form Data Preserved → User Clicks Save Again
```

## Toast Notification Hierarchy

```
┌─────────────────────────────────────────────────────────────┐
│                    Toast Priority Levels                     │
├─────────────────────────────────────────────────────────────┤
│  1. Loading (Manual Dismiss)                                │
│     - Blocks user action                                     │
│     - Must be explicitly removed                             │
│     - Blue spinner icon                                      │
├─────────────────────────────────────────────────────────────┤
│  2. Error (8 seconds)                                        │
│     - Requires user attention                                │
│     - Longer timeout for reading                             │
│     - Red alert icon                                         │
├─────────────────────────────────────────────────────────────┤
│  3. Warning (5 seconds)                                      │
│     - Important but not critical                             │
│     - Moderate timeout                                       │
│     - Amber warning icon                                     │
├─────────────────────────────────────────────────────────────┤
│  4. Success (3-5 seconds)                                    │
│     - Quick confirmation                                     │
│     - Shorter timeout                                        │
│     - Green check icon                                       │
├─────────────────────────────────────────────────────────────┤
│  5. Info (3 seconds)                                         │
│     - Informational only                                     │
│     - Quick dismiss                                          │
│     - Blue info icon                                         │
└─────────────────────────────────────────────────────────────┘
```

## State Management Flow

```
┌─────────────────────────────────────────────────────────────┐
│                    Component State                           │
├─────────────────────────────────────────────────────────────┤
│  localMediaType: 'image' | 'video'                          │
│  localImageUrl: string | null                               │
│  localVideoId: string | null                                │
│  youtubeUrl: string                                         │
│  youtubeUrlError: string | null                             │
│  imageUploading: boolean                                    │
│  validatingYoutubeUrl: boolean                              │
│  previewLoading: boolean                                    │
└─────────────────────┬───────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────┐
│                    State Transitions                         │
├─────────────────────────────────────────────────────────────┤
│  Idle → Loading → Success/Error → Idle                      │
│                                                              │
│  Error States:                                               │
│  - Preserve form data                                        │
│  - Show error message                                        │
│  - Enable retry                                              │
│                                                              │
│  Loading States:                                             │
│  - Disable controls                                          │
│  - Show progress indicator                                   │
│  - Prevent concurrent operations                             │
│                                                              │
│  Success States:                                             │
│  - Update form data                                          │
│  - Show confirmation                                         │
│  - Enable next action                                        │
└─────────────────────────────────────────────────────────────┘
```

## Accessibility Flow

```
┌─────────────────────────────────────────────────────────────┐
│                    User Interaction                          │
└─────────────────────┬───────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────┐
│              Visual Feedback                                 │
│              - Color-coded indicators                        │
│              - Icon indicators                               │
│              - Loading animations                            │
└─────────────────────┬───────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────┐
│              Screen Reader Announcements                     │
│              - Toast notifications announced                 │
│              - Error messages announced                      │
│              - Loading states announced                      │
└─────────────────────┬───────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────┐
│              Keyboard Navigation                             │
│              - All controls accessible                       │
│              - Focus indicators visible                      │
│              - Logical tab order                             │
└─────────────────────────────────────────────────────────────┘
```

## Legend

```
┌─────────────┐
│   Process   │  - Action or operation
└─────────────┘

┌─────────────┐
│   Decision  │  - Conditional branch
└─────────────┘

┌─────────────┐
│   Toast     │  - User notification
└─────────────┘

      │
      ▼          - Flow direction

Success│         - Conditional path
       ▼
```
