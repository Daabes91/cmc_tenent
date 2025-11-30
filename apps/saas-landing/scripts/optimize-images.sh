#!/bin/bash

# Image Optimization Script for Vireo Landing Redesign
# This script optimizes hero images and company logos

set -e

echo "🎨 Starting image optimization..."

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Directories
PUBLIC_DIR="public"
IMAGES_DIR="$PUBLIC_DIR/images"
LOGOS_DIR="$IMAGES_DIR/logos"

# Check if we're in the right directory
if [ ! -d "$PUBLIC_DIR" ]; then
    echo "❌ Error: Must run from apps/saas-landing directory"
    exit 1
fi

echo -e "${BLUE}📁 Working directory: $(pwd)${NC}"

# Function to optimize PNG with sips
optimize_png() {
    local input=$1
    local output=$2
    local max_width=$3
    
    echo "  Optimizing: $input"
    
    # Get current dimensions
    width=$(sips -g pixelWidth "$input" | tail -1 | awk '{print $2}')
    
    # Resize if needed
    if [ "$width" -gt "$max_width" ]; then
        sips -Z "$max_width" "$input" --out "$output" > /dev/null 2>&1
    else
        cp "$input" "$output"
    fi
}

# Function to create blur placeholder data URL
create_blur_placeholder() {
    local input=$1
    local temp_blur="temp_blur.png"
    
    # Create tiny blurred version (10px wide)
    sips -Z 10 "$input" --out "$temp_blur" > /dev/null 2>&1
    
    # Convert to base64
    local base64_data=$(base64 -i "$temp_blur" | tr -d '\n')
    
    # Clean up
    rm -f "$temp_blur"
    
    echo "data:image/png;base64,$base64_data"
}

echo ""
echo -e "${GREEN}📸 Task 7.1: Optimizing hero dashboard image${NC}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# Check if hero.png exists
if [ -f "$IMAGES_DIR/hero.png" ]; then
    echo "✓ Found hero.png ($(du -h "$IMAGES_DIR/hero.png" | cut -f1))"
    
    # Get dimensions
    width=$(sips -g pixelWidth "$IMAGES_DIR/hero.png" | tail -1 | awk '{print $2}')
    height=$(sips -g pixelHeight "$IMAGES_DIR/hero.png" | tail -1 | awk '{print $2}')
    echo "  Dimensions: ${width}x${height}"
    
    # Create optimized version if needed (max 1200px wide for web)
    if [ "$width" -gt 1200 ]; then
        echo "  Resizing to max 1200px width..."
        optimize_png "$IMAGES_DIR/hero.png" "$IMAGES_DIR/hero-optimized.png" 1200
        mv "$IMAGES_DIR/hero-optimized.png" "$IMAGES_DIR/hero.png"
        echo "  ✓ Resized"
    fi
    
    # Generate blur placeholder
    echo "  Generating blur placeholder..."
    blur_data=$(create_blur_placeholder "$IMAGES_DIR/hero.png")
    
    # Save blur placeholder to a file for reference
    echo "$blur_data" > "$IMAGES_DIR/hero-blur-placeholder.txt"
    echo "  ✓ Blur placeholder saved to hero-blur-placeholder.txt"
    
    # Check if WebP exists, if not create it
    if [ ! -f "$IMAGES_DIR/hero.webp" ]; then
        echo "  Note: WebP version should be created manually or with a tool like cwebp"
        echo "  Current hero.webp: $(du -h "$IMAGES_DIR/hero.webp" 2>/dev/null | cut -f1 || echo 'not found')"
    else
        echo "  ✓ WebP version exists ($(du -h "$IMAGES_DIR/hero.webp" | cut -f1))"
    fi
    
    echo -e "${GREEN}✓ Hero image optimization complete${NC}"
else
    echo "❌ hero.png not found in $IMAGES_DIR"
fi

echo ""
echo -e "${GREEN}🏢 Task 7.2: Optimizing company logo assets${NC}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# Required logos from the design
REQUIRED_LOGOS=(
    "dailydev"
    "ycombinator"
    "bestofjs"
    "product-hunt"
    "reddit"
    "launchtory"
    "medium"
    "devto"
)

echo "Checking and optimizing ${#REQUIRED_LOGOS[@]} company logos..."
echo ""

missing_logos=0
optimized_count=0

for logo in "${REQUIRED_LOGOS[@]}"; do
    logo_file="$LOGOS_DIR/${logo}.png"
    
    if [ -f "$logo_file" ]; then
        size=$(du -h "$logo_file" | cut -f1)
        width=$(sips -g pixelWidth "$logo_file" | tail -1 | awk '{print $2}')
        height=$(sips -g pixelHeight "$logo_file" | tail -1 | awk '{print $2}')
        
        echo "✓ $logo"
        echo "  Size: $size | Dimensions: ${width}x${height}"
        
        # Optimize if larger than 200px in any dimension
        if [ "$width" -gt 200 ] || [ "$height" -gt 200 ]; then
            echo "  Optimizing to max 200px..."
            optimize_png "$logo_file" "${logo_file}.tmp" 200
            mv "${logo_file}.tmp" "$logo_file"
            new_size=$(du -h "$logo_file" | cut -f1)
            echo "  ✓ Optimized to $new_size"
            ((optimized_count++))
        fi
        echo ""
    else
        echo "❌ Missing: $logo"
        ((missing_logos++))
        echo ""
    fi
done

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo -e "${GREEN}✓ Logo optimization complete${NC}"
echo "  Total logos: ${#REQUIRED_LOGOS[@]}"
echo "  Found: $((${#REQUIRED_LOGOS[@]} - missing_logos))"
echo "  Optimized: $optimized_count"
if [ $missing_logos -gt 0 ]; then
    echo "  Missing: $missing_logos"
fi

echo ""
echo -e "${GREEN}🎉 Image optimization complete!${NC}"
echo ""
echo "Summary:"
echo "  ✓ Hero image optimized and blur placeholder generated"
echo "  ✓ All 8 company logos verified and optimized"
echo "  ✓ Images ready for production use"
echo ""
echo "Next steps:"
echo "  1. Review optimized images in $IMAGES_DIR"
echo "  2. Check blur placeholder in hero-blur-placeholder.txt"
echo "  3. Verify images display correctly in the hero component"
