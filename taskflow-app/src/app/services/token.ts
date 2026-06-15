import Introspect from "@/@types/Introspect";

export default async function Token(token: string): Promise<Introspect> {
  try {
    // Decodificação manual do JWT para ser 100% seguro no Edge Runtime
    const parts = token.split('.');
    if (parts.length !== 3) return { active: false };

    const payload = JSON.parse(atob(parts[1].replace(/-/g, '+').replace(/_/g, '/')));
    
    const isExpired = payload.exp ? payload.exp * 1000 < Date.now() : true;

    if (isExpired) {
      return { active: false };
    }

    return {
      active: true,
      sub: payload.sub,
      exp: payload.exp,
      iat: payload.iat,
    };
  } catch (error) {
    console.error("Token validation error:", error);
    return { active: false };
  }
}
