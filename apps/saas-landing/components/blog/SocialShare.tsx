/**
 * Social Share Component
 * 
 * Provides social media sharing buttons for blog posts.
 */

'use client';

import { Facebook, Twitter, Linkedin, Link2, Mail } from 'lucide-react';
import { useState } from 'react';

interface SocialShareProps {
  url: string;
  title: string;
  description?: string;
}

export function SocialShare({ url, title, description }: SocialShareProps) {
  const [copied, setCopied] = useState(false);
  
  const encodedUrl = encodeURIComponent(url);
  const encodedTitle = encodeURIComponent(title);
  const encodedDescription = description ? encodeURIComponent(description) : '';
  
  const shareLinks = {
    twitter: `https://twitter.com/intent/tweet?url=${encodedUrl}&text=${encodedTitle}`,
    facebook: `https://www.facebook.com/sharer/sharer.php?u=${encodedUrl}`,
    linkedin: `https://www.linkedin.com/sharing/share-offsite/?url=${encodedUrl}`,
    email: `mailto:?subject=${encodedTitle}&body=${encodedDescription}%0A%0A${encodedUrl}`,
  };
  
  const handleCopyLink = async () => {
    try {
      await navigator.clipboard.writeText(url);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    } catch (err) {
      console.error('Failed to copy link:', err);
    }
  };
  
  const handleShare = (platform: keyof typeof shareLinks) => {
    window.open(shareLinks[platform], '_blank', 'noopener,noreferrer,width=600,height=400');
  };
  
  return (
    <div className="flex items-center gap-2">
      <span className="text-sm font-medium text-muted-foreground">Share:</span>
      
      <button
        onClick={() => handleShare('twitter')}
        className="p-2 rounded-full hover:bg-muted transition-colors"
        aria-label="Share on Twitter"
        title="Share on Twitter"
      >
        <Twitter className="w-5 h-5" />
      </button>
      
      <button
        onClick={() => handleShare('facebook')}
        className="p-2 rounded-full hover:bg-muted transition-colors"
        aria-label="Share on Facebook"
        title="Share on Facebook"
      >
        <Facebook className="w-5 h-5" />
      </button>
      
      <button
        onClick={() => handleShare('linkedin')}
        className="p-2 rounded-full hover:bg-muted transition-colors"
        aria-label="Share on LinkedIn"
        title="Share on LinkedIn"
      >
        <Linkedin className="w-5 h-5" />
      </button>
      
      <button
        onClick={() => handleShare('email')}
        className="p-2 rounded-full hover:bg-muted transition-colors"
        aria-label="Share via Email"
        title="Share via Email"
      >
        <Mail className="w-5 h-5" />
      </button>
      
      <button
        onClick={handleCopyLink}
        className="p-2 rounded-full hover:bg-muted transition-colors relative"
        aria-label="Copy link"
        title={copied ? 'Link copied!' : 'Copy link'}
      >
        <Link2 className="w-5 h-5" />
        {copied && (
          <span className="absolute -top-8 left-1/2 -translate-x-1/2 bg-foreground text-background text-xs px-2 py-1 rounded whitespace-nowrap">
            Copied!
          </span>
        )}
      </button>
    </div>
  );
}
