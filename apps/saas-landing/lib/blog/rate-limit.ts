/**
 * Rate Limiting for Blog Search
 * 
 * Prevents abuse of blog search functionality by limiting requests per user
 */

export interface RateLimitConfig {
  maxRequests: number;
  windowMs: number;
  message?: string;
}

export interface RateLimitResult {
  success: boolean;
  remaining: number;
  resetAt: number;
  message?: string;
}

/**
 * In-memory rate limiter
 * For production, consider using Redis or similar distributed cache
 */
export class RateLimiter {
  private requests: Map<string, number[]> = new Map();
  private config: RateLimitConfig;

  constructor(config: RateLimitConfig) {
    this.config = {
      maxRequests: config.maxRequests || 10,
      windowMs: config.windowMs || 60000, // 1 minute default
      message: config.message || 'Too many requests, please try again later',
    };

    // Clean up old entries periodically
    setInterval(() => this.cleanup(), this.config.windowMs);
  }

  /**
   * Check if request is allowed
   */
  check(identifier: string): RateLimitResult {
    const now = Date.now();
    const windowStart = now - this.config.windowMs;

    // Get existing requests for this identifier
    let timestamps = this.requests.get(identifier) || [];

    // Filter out requests outside the current window
    timestamps = timestamps.filter((timestamp) => timestamp > windowStart);

    // Check if limit exceeded
    if (timestamps.length >= this.config.maxRequests) {
      const oldestTimestamp = Math.min(...timestamps);
      const resetAt = oldestTimestamp + this.config.windowMs;

      return {
        success: false,
        remaining: 0,
        resetAt,
        message: this.config.message,
      };
    }

    // Add current request
    timestamps.push(now);
    this.requests.set(identifier, timestamps);

    return {
      success: true,
      remaining: this.config.maxRequests - timestamps.length,
      resetAt: now + this.config.windowMs,
    };
  }

  /**
   * Reset rate limit for identifier
   */
  reset(identifier: string): void {
    this.requests.delete(identifier);
  }

  /**
   * Clean up old entries
   */
  private cleanup(): void {
    const now = Date.now();
    const windowStart = now - this.config.windowMs;

    for (const [identifier, timestamps] of this.requests.entries()) {
      const validTimestamps = timestamps.filter((timestamp) => timestamp > windowStart);

      if (validTimestamps.length === 0) {
        this.requests.delete(identifier);
      } else {
        this.requests.set(identifier, validTimestamps);
      }
    }
  }

  /**
   * Get current stats
   */
  getStats(): {
    totalIdentifiers: number;
    totalRequests: number;
  } {
    let totalRequests = 0;

    for (const timestamps of this.requests.values()) {
      totalRequests += timestamps.length;
    }

    return {
      totalIdentifiers: this.requests.size,
      totalRequests,
    };
  }
}

/**
 * Blog search rate limiter instance
 * 10 requests per minute per IP/session
 */
const blogSearchRateLimiter = new RateLimiter({
  maxRequests: 10,
  windowMs: 60000, // 1 minute
  message: 'Too many search requests. Please wait a moment before searching again.',
});

/**
 * Check rate limit for blog search
 */
export function checkBlogSearchRateLimit(identifier: string): RateLimitResult {
  return blogSearchRateLimiter.check(identifier);
}

/**
 * Reset rate limit for blog search
 */
export function resetBlogSearchRateLimit(identifier: string): void {
  blogSearchRateLimiter.reset(identifier);
}

/**
 * Get blog search rate limit stats
 */
export function getBlogSearchRateLimitStats() {
  return blogSearchRateLimiter.getStats();
}

/**
 * Get identifier for rate limiting
 * Uses IP address if available, otherwise falls back to session ID
 */
export function getRateLimitIdentifier(request?: Request): string {
  if (typeof window !== 'undefined') {
    // Client-side: use session ID from localStorage
    let sessionId = localStorage.getItem('sessionId');

    if (!sessionId) {
      sessionId = generateSessionId();
      localStorage.setItem('sessionId', sessionId);
    }

    return sessionId;
  }

  // Server-side: use IP address from request
  if (request) {
    const forwarded = request.headers.get('x-forwarded-for');
    const ip = forwarded ? forwarded.split(',')[0] : request.headers.get('x-real-ip') || 'unknown';
    return ip;
  }

  return 'unknown';
}

/**
 * Generate unique session ID
 */
function generateSessionId(): string {
  return `${Date.now()}-${Math.random().toString(36).substring(2, 15)}`;
}

/**
 * Rate limit middleware for API routes
 */
export function withRateLimit(
  handler: (request: Request) => Promise<Response>,
  limiter: RateLimiter = blogSearchRateLimiter
) {
  return async (request: Request): Promise<Response> => {
    const identifier = getRateLimitIdentifier(request);
    const result = limiter.check(identifier);

    if (!result.success) {
      return new Response(
        JSON.stringify({
          error: result.message,
          resetAt: result.resetAt,
        }),
        {
          status: 429,
          headers: {
            'Content-Type': 'application/json',
            'X-RateLimit-Limit': limiter['config'].maxRequests.toString(),
            'X-RateLimit-Remaining': result.remaining.toString(),
            'X-RateLimit-Reset': result.resetAt.toString(),
            'Retry-After': Math.ceil((result.resetAt - Date.now()) / 1000).toString(),
          },
        }
      );
    }

    // Add rate limit headers to response
    const response = await handler(request);

    response.headers.set('X-RateLimit-Limit', limiter['config'].maxRequests.toString());
    response.headers.set('X-RateLimit-Remaining', result.remaining.toString());
    response.headers.set('X-RateLimit-Reset', result.resetAt.toString());

    return response;
  };
}

/**
 * React hook for rate limiting
 */
export function useRateLimit(identifier?: string) {
  const id = identifier || getRateLimitIdentifier();

  const checkLimit = (): RateLimitResult => {
    return checkBlogSearchRateLimit(id);
  };

  const resetLimit = (): void => {
    resetBlogSearchRateLimit(id);
  };

  return {
    checkLimit,
    resetLimit,
    identifier: id,
  };
}
