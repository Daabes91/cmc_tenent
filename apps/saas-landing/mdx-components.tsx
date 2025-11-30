/**
 * MDX Components
 * 
 * Custom components for rendering MDX content.
 */

import type { MDXComponents } from 'mdx/types';

export function useMDXComponents(components: MDXComponents): MDXComponents {
  return {
    // Customize heading components
    h1: ({ children }) => (
      <h1 className="text-4xl font-bold mt-8 mb-4 scroll-mt-20">{children}</h1>
    ),
    h2: ({ children }) => (
      <h2 className="text-3xl font-bold mt-8 mb-4 scroll-mt-20">{children}</h2>
    ),
    h3: ({ children }) => (
      <h3 className="text-2xl font-bold mt-6 mb-3 scroll-mt-20">{children}</h3>
    ),
    h4: ({ children }) => (
      <h4 className="text-xl font-bold mt-4 mb-2 scroll-mt-20">{children}</h4>
    ),
    
    // Customize paragraph
    p: ({ children }) => (
      <p className="mb-4 leading-7">{children}</p>
    ),
    
    // Customize lists
    ul: ({ children }) => (
      <ul className="list-disc list-inside mb-4 space-y-2">{children}</ul>
    ),
    ol: ({ children }) => (
      <ol className="list-decimal list-inside mb-4 space-y-2">{children}</ol>
    ),
    li: ({ children }) => (
      <li className="leading-7">{children}</li>
    ),
    
    // Customize links
    a: ({ href, children }) => (
      <a
        href={href}
        className="text-primary hover:underline"
        target={href?.startsWith('http') ? '_blank' : undefined}
        rel={href?.startsWith('http') ? 'noopener noreferrer' : undefined}
      >
        {children}
      </a>
    ),
    
    // Customize code blocks
    code: ({ children }) => (
      <code className="bg-muted px-1.5 py-0.5 rounded text-sm font-mono">
        {children}
      </code>
    ),
    pre: ({ children }) => (
      <pre className="bg-muted p-4 rounded-lg overflow-x-auto mb-4">
        {children}
      </pre>
    ),
    
    // Customize blockquotes
    blockquote: ({ children }) => (
      <blockquote className="border-l-4 border-primary pl-4 italic my-4">
        {children}
      </blockquote>
    ),
    
    // Customize horizontal rule
    hr: () => (
      <hr className="my-8 border-border" />
    ),
    
    // Customize images
    img: ({ src, alt }) => (
      <img
        src={src}
        alt={alt || ''}
        className="rounded-lg my-4 w-full h-auto"
      />
    ),
    
    // Customize tables
    table: ({ children }) => (
      <div className="overflow-x-auto my-4">
        <table className="min-w-full divide-y divide-border">
          {children}
        </table>
      </div>
    ),
    th: ({ children }) => (
      <th className="px-4 py-2 text-left font-semibold bg-muted">
        {children}
      </th>
    ),
    td: ({ children }) => (
      <td className="px-4 py-2 border-t border-border">
        {children}
      </td>
    ),
    
    ...components,
  };
}
