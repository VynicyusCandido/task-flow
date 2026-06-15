import { NextResponse, NextRequest } from 'next/server';
import Token from './app/services/token';
import { Auth } from './app/enums';
import Introspect from './@types/Introspect';

export async function proxy(request: NextRequest) {
  try {
    const token = request.cookies.get(Auth.AUTH_TOKEN)?.value;
    
    if (token) {
      const introspect: Introspect = await Token(token);
      if (introspect?.active) {
        return NextResponse.next();
      }
    }

    // Se o token for inválido ou não existir, redireciona para login e limpa o cookie
    const response = NextResponse.redirect(new URL('/login', request.url));
    
    if (token) {
      response.cookies.delete(Auth.AUTH_TOKEN);
    }
    
    return response;
  } catch (error) {
    console.error('Middleware execution failed:', error);
    // Em caso de erro crítico no middleware, permite a requisição para evitar travamento total
    // ou redireciona para login se for uma rota protegida
    return NextResponse.next();
  }
}

export default async function middleware(request: NextRequest) {
  return proxy(request);
}

export const config = {
  matcher: ['/', '/dashboard/:path*']
}
