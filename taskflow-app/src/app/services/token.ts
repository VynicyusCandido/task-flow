import Introspect from "@/@types/Introspect";
import { jwtDecode } from "jwt-decode";

export default async function Token(token: string): Promise<Introspect> {
  try {
    const decoded = jwtDecode(token);
    const isExpired = decoded.exp ? decoded.exp * 1000 < Date.now() : true;

    if (isExpired) {
      return { active: false };
    }

    return {
      active: true,
      sub: decoded.sub,
      exp: decoded.exp,
      iat: decoded.iat,
    };
  } catch {
    return { active: false };
  }
}
