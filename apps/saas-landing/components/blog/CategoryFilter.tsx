/**
 * Category Filter Component
 * 
 * Allows users to filter blog posts by category.
 */

'use client';

import { BlogCategory } from '@/lib/blog/types';
import { Button } from '@/components/ui/button';

interface CategoryFilterProps {
  categories: Record<BlogCategory, number>;
  selectedCategory: BlogCategory | 'all';
  onCategoryChange: (category: BlogCategory | 'all') => void;
}

const categoryLabels: Record<BlogCategory | 'all', string> = {
  'all': 'All Posts',
  'practice-management': 'Practice Management',
  'patient-care': 'Patient Care',
  'technology': 'Technology',
  'compliance': 'Compliance',
  'industry-news': 'Industry News',
};

export function CategoryFilter({ categories, selectedCategory, onCategoryChange }: CategoryFilterProps) {
  const allCount = Object.values(categories).reduce((sum, count) => sum + count, 0);

  return (
    <div className="flex flex-wrap gap-2">
      <Button
        variant={selectedCategory === 'all' ? 'default' : 'outline'}
        size="sm"
        onClick={() => onCategoryChange('all')}
        className="rounded-full"
      >
        {categoryLabels['all']} ({allCount})
      </Button>
      
      {(Object.keys(categories) as BlogCategory[]).map((category) => (
        <Button
          key={category}
          variant={selectedCategory === category ? 'default' : 'outline'}
          size="sm"
          onClick={() => onCategoryChange(category)}
          className="rounded-full"
        >
          {categoryLabels[category]} ({categories[category]})
        </Button>
      ))}
    </div>
  );
}
