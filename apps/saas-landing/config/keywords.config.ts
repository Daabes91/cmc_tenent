/**
 * Keyword Strategy Configuration
 * Defines primary, long-tail, and local keywords for SEO optimization
 */

export interface KeywordData {
  volume: 'high' | 'medium' | 'low';
  difficulty: 'high' | 'medium' | 'low';
  intent: 'commercial' | 'informational' | 'navigational' | 'transactional';
  pages: string[];
}

export interface KeywordStrategy {
  primary: Record<string, KeywordData>;
  longTail: Record<string, KeywordData>;
  local: Record<string, KeywordData & { template?: boolean }>;
}

export const keywordStrategy: KeywordStrategy = {
  // Primary Keywords - High volume, high competition
  primary: {
    'clinic management software': {
      volume: 'high',
      difficulty: 'medium',
      intent: 'commercial',
      pages: ['/', '/features'],
    },
    'medical practice management': {
      volume: 'high',
      difficulty: 'high',
      intent: 'commercial',
      pages: ['/', '/features'],
    },
    'appointment scheduling software': {
      volume: 'medium',
      difficulty: 'medium',
      intent: 'commercial',
      pages: ['/features', '/pricing'],
    },
    'patient management system': {
      volume: 'medium',
      difficulty: 'medium',
      intent: 'commercial',
      pages: ['/', '/features'],
    },
    'healthcare software': {
      volume: 'high',
      difficulty: 'high',
      intent: 'commercial',
      pages: ['/', '/about'],
    },
    'medical billing software': {
      volume: 'medium',
      difficulty: 'medium',
      intent: 'commercial',
      pages: ['/features', '/pricing'],
    },
    'EMR software': {
      volume: 'medium',
      difficulty: 'high',
      intent: 'commercial',
      pages: ['/features'],
    },
  },

  // Long-tail Keywords - Lower volume, lower competition, higher conversion
  longTail: {
    'best clinic management software for small practices': {
      volume: 'low',
      difficulty: 'low',
      intent: 'commercial',
      pages: ['/blog/best-clinic-software', '/pricing'],
    },
    'how to manage clinic appointments efficiently': {
      volume: 'low',
      difficulty: 'low',
      intent: 'informational',
      pages: ['/blog/appointment-management-tips'],
    },
    'affordable medical practice management software': {
      volume: 'low',
      difficulty: 'low',
      intent: 'commercial',
      pages: ['/pricing', '/features'],
    },
    'cloud-based clinic management system': {
      volume: 'low',
      difficulty: 'low',
      intent: 'commercial',
      pages: ['/', '/features'],
    },
    'multi-location clinic management software': {
      volume: 'low',
      difficulty: 'low',
      intent: 'commercial',
      pages: ['/features'],
    },
    'HIPAA compliant patient management software': {
      volume: 'low',
      difficulty: 'low',
      intent: 'commercial',
      pages: ['/features', '/security'],
    },
    'clinic software with online booking': {
      volume: 'low',
      difficulty: 'low',
      intent: 'commercial',
      pages: ['/features'],
    },
    'medical practice software comparison': {
      volume: 'low',
      difficulty: 'low',
      intent: 'informational',
      pages: ['/blog/software-comparison'],
    },
  },

  // Local Keywords - Geographic targeting
  local: {
    'clinic management software in [city]': {
      volume: 'low',
      difficulty: 'low',
      intent: 'commercial',
      pages: ['/locations/[city]'],
      template: true,
    },
    'medical practice management [region]': {
      volume: 'low',
      difficulty: 'low',
      intent: 'commercial',
      pages: ['/locations/[region]'],
      template: true,
    },
    'healthcare software providers near me': {
      volume: 'low',
      difficulty: 'low',
      intent: 'commercial',
      pages: ['/locations'],
    },
  },
};

/**
 * Get keywords for a specific page
 */
export function getKeywordsForPage(pagePath: string): string[] {
  const keywords: string[] = [];

  // Check primary keywords
  Object.entries(keywordStrategy.primary).forEach(([keyword, data]) => {
    if (data.pages.includes(pagePath)) {
      keywords.push(keyword);
    }
  });

  // Check long-tail keywords
  Object.entries(keywordStrategy.longTail).forEach(([keyword, data]) => {
    if (data.pages.includes(pagePath)) {
      keywords.push(keyword);
    }
  });

  // Check local keywords
  Object.entries(keywordStrategy.local).forEach(([keyword, data]) => {
    if (data.pages.includes(pagePath)) {
      keywords.push(keyword);
    }
  });

  return keywords;
}

/**
 * Get all keywords by intent
 */
export function getKeywordsByIntent(
  intent: 'commercial' | 'informational' | 'navigational' | 'transactional'
): string[] {
  const keywords: string[] = [];

  Object.entries({ ...keywordStrategy.primary, ...keywordStrategy.longTail }).forEach(
    ([keyword, data]) => {
      if (data.intent === intent) {
        keywords.push(keyword);
      }
    }
  );

  return keywords;
}
