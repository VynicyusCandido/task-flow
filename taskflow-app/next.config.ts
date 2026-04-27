const isDevelopment = process.env.NEXT_PUBLIC_APP_ENV !== 'production';

const getAllowedOriginsByEnvironment = () => {
  return isDevelopment ? 'http://localhost:3000' : 'https://taskflow.app';
};

const securityHeaders = [
  {
    // Headers de segurança para arquivos estáticos (fonts, JS, CSS, imagens)
    source: '/_next/static/(.*)',
    headers: [
      { key: 'X-Content-Type-Options', value: 'nosniff' },
      { key: 'X-Frame-Options', value: 'DENY' },
      { key: 'X-XSS-Protection', value: '1; mode=block' },
      { key: 'Referrer-Policy', value: 'strict-origin-when-cross-origin' },
      { key: 'Cache-Control', value: 'public, max-age=31536000, immutable' }
    ],
  },
  {
    // Headers para imagens otimizadas do Next.js
    source: '/_next/image/(.*)',
    headers: [
      { key: 'X-Content-Type-Options', value: 'nosniff' },
      { key: 'Cache-Control', value: 'public, max-age=31536000, immutable' }
    ],
  },
  {
    source: '/(.*)',
    headers: [
      {
        key: 'Content-Security-Policy',
        value: `
              default-src 'self';
              script-src 'self' 'unsafe-inline' 'unsafe-eval';
              style-src 'self' 'unsafe-inline';
              img-src 'self' data: blob:;
              font-src 'self' data:;
              connect-src 'self' http://localhost:8080;
              frame-src 'none';
              object-src 'none';
              base-uri 'self';
              form-action 'self';
              frame-ancestors 'self';
            `.replace(/\s{2,}/g, ' ').trim()
      },
      {
        key: 'Access-Control-Allow-Origin',
        value: `${getAllowedOriginsByEnvironment()}`
      },
      {
        key: 'Access-Control-Allow-Methods',
        value: 'GET, POST, PUT, OPTIONS, DELETE'
      },
      {
        key: 'Access-Control-Allow-Headers',
        value: 'Content-Type, Authorization'
      },
      { key: 'X-Content-Type-Options', value: 'nosniff' },
      { key: 'X-Frame-Options', value: 'DENY' },
      { key: 'X-XSS-Protection', value: '1; mode=block' },
      { key: 'Referrer-Policy', value: 'strict-origin-when-cross-origin' },
      { key: 'Strict-Transport-Security', value: 'max-age=31536000; includeSubDomains' }
    ],
  },
];

import type { NextConfig } from 'next';

const nextConfig: NextConfig = {
  async headers() {
    return isDevelopment ? [] : securityHeaders;
  },
  async rewrites() {
    return [
      {
        source: '/api/:path*',
        destination: 'http://localhost:8080/api/:path*'
      }
    ];
  }
};

export default nextConfig;
