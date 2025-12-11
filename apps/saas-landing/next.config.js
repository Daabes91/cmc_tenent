/* eslint-disable @typescript-eslint/no-require-imports */
const path = require("path");
const loaderUtils = require("loader-utils");
const createMDX = require('@next/mdx');
const remarkGfm = require('remark-gfm');
const rehypeHighlight = require('rehype-highlight');

const hashOnlyIdent = (context, _, exportName) =>
  loaderUtils
    .getHashDigest(
      Buffer.from(
        `filePath:${path
          .relative(context.rootContext, context.resourcePath)
          .replace(/\\+/g, "/")}#className:${exportName}`
      ),
      "md4",
      "base64",
      6
    )
    .replace(/[^a-zA-Z0-9-_]/g, "_")
    .replace(/^(-?\d|--)/, "_$1");

// Hardcode basePath to /landing so assets are served under the correct prefix.
const basePath = '/landing';

/** @type {import('next').NextConfig} */
const nextConfig = {
  // Configure MDX page extensions
  pageExtensions: ['js', 'jsx', 'ts', 'tsx', 'md', 'mdx'],
  reactStrictMode: true,
  // Serve the app behind a path prefix when deployed under a subpath (e.g. /landing)
  basePath,
  assetPrefix: basePath || undefined,
  // Add image optimization configuration
  images: {
    unoptimized: true,
    deviceSizes: [640, 750, 828, 1080, 1200, 1920, 2048],
    imageSizes: [16, 32, 48, 64, 96, 128, 256, 384],
    formats: ['image/webp', 'image/avif'],
    minimumCacheTTL: 31536000, // 1 year
    dangerouslyAllowSVG: true,
    contentDispositionType: 'attachment',
    contentSecurityPolicy: "default-src 'self'; script-src 'none'; sandbox;",
    remotePatterns: [
      {
        protocol: 'https',
        hostname: 'cdn.simpleicons.org',
      },
      {
        protocol: 'https',
        hostname: 'images.unsplash.com',
      },
    ],
  },
  // Update experimental settings to disable scroll restoration
  experimental: {
    optimizeCss: true,
    optimizePackageImports: [
      'motion',
      'framer-motion',
      '@tabler/icons-react'
    ],
    // Disable scroll restoration to prevent auto-scrolling on refresh
    scrollRestoration: false,
  },
  // Add compiler options for production
  compiler: {
    removeConsole: process.env.NODE_ENV === 'production',
  },
  async redirects() {
    // Gracefully handle legacy PayPal return URLs that omit the /landing basePath.
    return [
      {
        source: '/payment-confirmation',
        destination: `${basePath}/payment-confirmation`,
        permanent: false,
      },
      {
        source: '/payment-confirmation/:path*',
        destination: `${basePath}/payment-confirmation/:path*`,
        permanent: false,
      },
    ];
  },
  // Keep existing webpack configuration
  webpack(config, { dev }) {
    const rules = config.module.rules
      .find((rule) => typeof rule.oneOf === "object")
      .oneOf.filter((rule) => Array.isArray(rule.use));
    if (!dev)
      rules.forEach((rule) => {
        rule.use.forEach((moduleLoader) => {
          if (
            moduleLoader.loader?.includes("css-loader") &&
            !moduleLoader.loader?.includes("postcss-loader") &&
            moduleLoader?.options?.modules
          )
            moduleLoader.options.modules.getLocalIdent = hashOnlyIdent;
        });
      });

    return config;
  },
};

// Configure MDX with plugins
const withMDX = createMDX({
  extension: /\.mdx?$/,
  options: {
    remarkPlugins: [remarkGfm],
    rehypePlugins: [rehypeHighlight],
  },
});

module.exports = withMDX(nextConfig);
