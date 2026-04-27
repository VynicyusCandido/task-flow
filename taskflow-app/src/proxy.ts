import { NextResponse, NextRequest } from 'next/server';
import Token from '@/app/services/token';
import { Auth } from '@/app/enums';
import Introspect from '@/@types/Introspect';

export async function proxy(request: NextRequest) {
  const token = request.cookies.get(Auth.AUTH_TOKEN)?.value;
  if (token) {
    const introspect: Introspect = await Token(token);
    if (introspect?.active) {
      return NextResponse.next();
    }
  }
  return NextResponse.redirect(new URL('/login', request.url));
}

export default async function middleware(request: NextRequest) {
  return proxy(request);
}

export const config = {
  matcher: ['/', '/dashboard/:path*']
}
