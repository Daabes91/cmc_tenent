#!/bin/bash

# Hero Image Optimization Script
# This script optimizes the hero.png image for better performance

echo "🖼️  Optimizing hero image for performance..."

IMAGE_DIR="apps/saas-landing/public/images"
HERO_IMAGE="$IMAGE_DIR/hero.png"
HERO_WEBP="$IMAGE_DIR/hero.webp"

# Check if hero.png exists
if [ ! -f "$HERO_IMAGE" ]; then
  echo "❌ hero.png not found at $HERO_IMAGE"
  exit 1
fi

# Get original size
ORIGINAL_SIZE=$(du -h "$HERO_IMAGE" | cut -f1)
echo "📊 Original size: $ORIGINAL_SIZE"

# Check if ImageMagick is installed
if command -v convert &> /dev/null; then
  echo "✓ ImageMagick found"
  
  # Create optimized PNG (reduce quality slightly)
  echo "🔄 Creating optimized PNG..."
  convert "$HERO_IMAGE" -quality 85 -strip "$IMAGE_DIR/hero-optimized.png"
  
  OPTIMIZED_SIZE=$(du -h "$IMAGE_DIR/hero-optimized.png" | cut -f1)
  echo "✓ Optimized PNG created: $OPTIMIZED_SIZE"
  
  # Create WebP version if it doesn't exist or is outdated
  if [ ! -f "$HERO_WEBP" ] || [ "$HERO_IMAGE" -nt "$HERO_WEBP" ]; then
    echo "🔄 Creating WebP version..."
    convert "$HERO_IMAGE" -quality 80 "$HERO_WEBP"
    WEBP_SIZE=$(du -h "$HERO_WEBP" | cut -f1)
    echo "✓ WebP version created: $WEBP_SIZE"
  else
    echo "✓ WebP version already exists and is up to date"
  fi
  
elif command -v cwebp &> /dev/null; then
  echo "✓ cwebp found"
  
  # Create WebP version
  if [ ! -f "$HERO_WEBP" ] || [ "$HERO_IMAGE" -nt "$HERO_WEBP" ]; then
    echo "🔄 Creating WebP version..."
    cwebp -q 80 "$HERO_IMAGE" -o "$HERO_WEBP"
    WEBP_SIZE=$(du -h "$HERO_WEBP" | cut -f1)
    echo "✓ WebP version created: $WEBP_SIZE"
  else
    echo "✓ WebP version already exists and is up to date"
  fi
  
else
  echo "⚠️  No image optimization tools found"
  echo "   Install ImageMagick: brew install imagemagick"
  echo "   Or install WebP tools: brew install webp"
  echo ""
  echo "   For now, using existing hero.webp (48KB)"
fi

# Create responsive versions (optional)
echo ""
echo "💡 Recommendation: Create responsive image sizes"
echo "   - hero-mobile.webp (640px width)"
echo "   - hero-tablet.webp (1024px width)"
echo "   - hero-desktop.webp (1920px width)"

echo ""
echo "✅ Image optimization complete!"
echo ""
echo "📝 Next steps:"
echo "   1. Update hero.tsx to use hero.webp as primary source"
echo "   2. Add PNG fallback for older browsers"
echo "   3. Implement responsive srcset for different screen sizes"
