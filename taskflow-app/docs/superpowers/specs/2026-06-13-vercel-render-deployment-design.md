# Spec: Vercel Deployment and Render Backend Integration

**Status:** Draft
**Date:** 2026-06-13
**Topic:** Deployment of `taskflow-app` to Vercel with direct connection to `taskflow-api` on Render.

## 1. Goal
Configure the application to support two distinct environments:
- **Development (`npm run dev`)**: Connects to a local backend at `http://localhost:8080`.
- **Production/Staging (`npm run build && npm run start` or Vercel)**: Connects directly to the production backend on Render.

## 2. Architecture
The application will use Approach 2 (Direct Backend Calls) to communicate with the API. This avoids unnecessary proxy overhead and utilizes environment variables for flexibility.

### Data Flow
- **Client/Server Side**: Calls `NEXT_PUBLIC_API_URL` directly.
- **Next.js Config**: Dynamically adjusts Security Headers (CSP) based on the environment.

## 3. Implementation Details

### 3.1 Environment Variables
We will use Next.js native `.env` support:
- `.env.development`: 
  ```env
  NEXT_PUBLIC_API_URL=http://localhost:8080
  NEXT_PUBLIC_APP_ENV=development
  ```
- `.env.production` (for local production testing):
  ```env
  NEXT_PUBLIC_API_URL=https://taskflow-api.onrender.com
  NEXT_PUBLIC_APP_ENV=production
  ```
- **Vercel Dashboard**: Add these keys in the project settings.

### 3.2 `next.config.ts` Updates
- **Dynamic CSP**: The `connect-src` directive will be updated to use `process.env.NEXT_PUBLIC_API_URL` to allow outgoing connections to the correct backend.
- **Rewrite Removal**: Remove the `/api/:path*` rewrite as it's no longer needed for direct calls.
- **Header Logic**: Ensure headers are applied correctly in production mode.

### 3.3 `src/lib/api.ts` Review
- Ensure `fetchApi` uses `process.env.NEXT_PUBLIC_API_URL` without hardcoded fallbacks that could leak into production.

## 4. Verification Plan
1. **Local Dev**: Run `npm run dev` and verify it connects to localhost.
2. **Local Prod Test**: Create `.env.production`, run `npm run build && npm run start`, and verify it connects to Render.
3. **Deployment**: Trigger Vercel deploy and verify live application.

## 5. Security Considerations
- **CORS**: The Render backend MUST be configured to allow the Vercel domain and the local production testing domain.
- **CSP**: Restricts connections only to the designated API URL, preventing unauthorized data exfiltration.
