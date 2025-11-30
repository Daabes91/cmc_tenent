# Blog Draft & Scheduling Quick Reference

## Draft Posts

### Create a Draft
```yaml
draft: true
```

### Publish a Draft
```yaml
draft: false  # or remove the line
```

### Draft Behavior
- ❌ Not in blog listing
- ❌ Not accessible via URL
- ❌ Not in search results
- ❌ Not in sitemap

## Scheduled Posts

### Schedule a Post
```yaml
publishedAt: "2025-12-31"  # Future date
draft: false
```

### Scheduled Behavior
- ❌ Not visible until date
- ✅ Auto-publishes on date
- ❌ Not in search until published
- ❌ Not in sitemap until published

## Date Formats

```yaml
publishedAt: "2024-01-15"              # Date only
publishedAt: "2024-01-15T10:00:00"     # With time
publishedAt: "2024-01-15T10:00:00Z"    # With timezone
```

## Common Patterns

### Work in Progress
```yaml
title: "Draft: My Article"
publishedAt: "2024-01-15"
draft: true
```

### Schedule for Launch
```yaml
title: "Product Launch Announcement"
publishedAt: "2025-03-01T09:00:00"
draft: false
```

### Draft + Future Date
```yaml
publishedAt: "2025-12-31"
draft: true  # Won't publish even after date
```

## Testing

```bash
# Run tests
pnpm test:run lib/blog/__tests__/draft-scheduling.test.ts

# Create test posts
pnpm blog:create

# List all posts (including drafts)
pnpm blog:list
```

## Troubleshooting

| Issue | Solution |
|-------|----------|
| Draft is visible | Check `draft: true` in frontmatter |
| Scheduled post published early | Verify future date format |
| Post not appearing after date | Check `draft` is not `true` |
| 404 on valid post | Clear cache: `rm -rf .next` |

## Quick Commands

```bash
# Create new post
pnpm blog:create

# Validate post frontmatter
pnpm blog:validate <slug>

# List all posts
pnpm blog:list

# Run draft/scheduling tests
pnpm test:run lib/blog/__tests__/draft-scheduling.test.ts
```

## Requirements

✅ **Requirement 4.3**: Draft posts filtered from public listings  
✅ **Requirement 4.4**: Scheduled posts don't appear until publication date

## Property Tests

✅ **Property 7**: Draft Post Visibility  
✅ **Property 8**: Scheduled Post Visibility
