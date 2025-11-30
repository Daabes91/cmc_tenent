/**
 * Search Bar Component
 * 
 * Allows users to search blog posts by keyword with real-time feedback.
 */

'use client';

import { useState, useCallback, useEffect } from 'react';
import { Search, X, Loader2 } from 'lucide-react';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';

interface SearchBarProps {
  onSearch: (keyword: string) => void;
  placeholder?: string;
}

export function SearchBar({ onSearch, placeholder = 'Search articles...' }: SearchBarProps) {
  const [searchTerm, setSearchTerm] = useState('');
  const [isSearching, setIsSearching] = useState(false);

  const handleSearch = useCallback(() => {
    if (searchTerm.trim()) {
      setIsSearching(true);
      onSearch(searchTerm.trim());
      // Reset searching state after a brief delay
      setTimeout(() => setIsSearching(false), 300);
    } else {
      onSearch('');
    }
  }, [searchTerm, onSearch]);

  const handleClear = useCallback(() => {
    setSearchTerm('');
    onSearch('');
  }, [onSearch]);

  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter') {
      e.preventDefault();
      handleSearch();
    } else if (e.key === 'Escape') {
      handleClear();
    }
  };

  // Auto-search with debounce for better UX
  useEffect(() => {
    const timer = setTimeout(() => {
      if (searchTerm.trim()) {
        onSearch(searchTerm.trim());
      }
    }, 500);

    return () => clearTimeout(timer);
  }, [searchTerm, onSearch]);

  return (
    <div className="relative w-full max-w-2xl">
      <div className="relative">
        <div className="absolute left-3 top-1/2 -translate-y-1/2">
          {isSearching ? (
            <Loader2 className="h-5 w-5 text-primary animate-spin" />
          ) : (
            <Search className="h-5 w-5 text-slate-400 dark:text-gray-500" />
          )}
        </div>
        <Input
          type="text"
          placeholder={placeholder}
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          onKeyDown={handleKeyDown}
          className="pl-10 pr-20 h-12 text-base border-slate-200 dark:border-gray-700 focus:border-primary focus:ring-primary"
          aria-label="Search blog posts"
        />
        {searchTerm && (
          <Button
            variant="ghost"
            size="sm"
            onClick={handleClear}
            className="absolute right-16 top-1/2 -translate-y-1/2 h-8 w-8 p-0 hover:bg-slate-100 dark:hover:bg-gray-800"
            aria-label="Clear search"
          >
            <X className="h-4 w-4" />
          </Button>
        )}
        <Button
          onClick={handleSearch}
          className="absolute right-1 top-1/2 -translate-y-1/2 h-10 bg-primary hover:bg-primary/90"
          disabled={isSearching}
          aria-label="Search"
        >
          {isSearching ? (
            <Loader2 className="h-4 w-4 animate-spin" />
          ) : (
            'Search'
          )}
        </Button>
      </div>
      {searchTerm && (
        <p className="text-xs text-slate-500 dark:text-gray-400 mt-2">
          Press Enter to search or Escape to clear
        </p>
      )}
    </div>
  );
}
