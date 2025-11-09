#!/bin/bash

# Clear Nuxt cache and rebuild
echo "Clearing Nuxt cache..."

# Remove .nuxt directory
rm -rf .nuxt

# Remove .output directory
rm -rf .output

# Remove node_modules/.cache
rm -rf node_modules/.cache

echo "Cache cleared! Now run: npm run dev"
