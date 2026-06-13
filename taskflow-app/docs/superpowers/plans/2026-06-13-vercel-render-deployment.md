# Vercel Deployment and Render Backend Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Configure the application to connect to the Render backend in production and localhost in development, ensuring security via CSP and environment variables.

**Architecture:** Use Approach 2 (Direct Backend Calls). Next.js will use `.env.development` for local dev and `.env.production` (or Vercel settings) for production. The `next.config.ts` will dynamically adjust the CSP based on `NEXT_PUBLIC_API_URL`.

**Tech Stack:** Next.js, TypeScript, Environment Variables.

---

### Task 1: Create Environment Variable Files

**Files:**
- Create: `.env.development`
- Create: `.env.production`

- [ ] **Step 1: Create .env.development**

```env
NEXT_PUBLIC_API_URL=http://localhost:8080
NEXT_PUBLIC_APP_ENV=development
```

- [ ] **Step 2: Create .env.production**

```env
NEXT_PUBLIC_API_URL=https://taskflow-api.onrender.com
NEXT_PUBLIC_APP_ENV=production
```

- [ ] **Step 3: Commit environment templates**

*Note: Since these are environment files, we usually commit `.env.example`. I will create it as well.*

```bash
cp .env.development .env.example
git add .env.example
git commit -m "chore: add environment variable templates"
```

---

### Task 2: Update Next.js Configuration

**Files:**
- Modify: `next.config.ts`

- [ ] **Step 1: Make CSP dynamic and remove rewrites**

Update the logic to use `process.env.NEXT_PUBLIC_API_URL` in the CSP and remove the `rewrites` function.

```typescript
const isDevelopment = process.env.NEXT_PUBLIC_APP_ENV !== 'production';
const apiUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

const getAllowedOriginsByEnvironment = () => {
  return isDevelopment ? 'http://localhost:3000' : 'https://taskflow.app';
};

const securityHeaders = [
  // ... existing headers ...
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
              connect-src 'self' ${apiUrl};
              frame-src 'none';
              object-src 'none';
              base-uri 'self';
              form-action 'self';
              frame-ancestors 'self';
            `.replace(/\s{2,}/g, ' ').trim()
      },
      // ... rest of headers ...
    ],
  },
];

import type { NextConfig } from 'next';

const nextConfig: NextConfig = {
  async headers() {
    return isDevelopment ? [] : securityHeaders;
  },
  // rewrites removed
};

export default nextConfig;
```

- [ ] **Step 2: Commit Next.js config changes**

```bash
git add next.config.ts
git commit -m "config: dynamic CSP and remove legacy rewrites"
```

---

### Task 3: Refine API Service

**Files:**
- Modify: `src/lib/api.ts`

- [ ] **Step 1: Ensure API_BASE_URL is purely environment-driven**

```typescript
// src/lib/api.ts
const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL;

if (!API_BASE_URL) {
  throw new Error("NEXT_PUBLIC_API_URL is not defined");
}
```

- [ ] **Step 2: Commit API service changes**

```bash
git add src/lib/api.ts
git commit -m "refactor: ensure API URL is strictly environment-driven"
```

---

### Task 4: Verification

- [ ] **Step 1: Verify Development Mode**

Run: `npm run dev`
Check: Browser console or Network tab. API calls should go to `http://localhost:8080`.

- [ ] **Step 2: Verify Production Mode (Local)**

Run: `npm run build && npm run start`
Check: Browser Network tab. API calls should go to `https://taskflow-api.onrender.com`. CSP should allow it.

- [ ] **Step 3: Cleanup documentation**

```bash
rm brainstorming_tasks.md
git add .
git commit -m "docs: cleanup brainstorming tasks"
```
