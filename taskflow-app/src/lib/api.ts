import { cookies } from "next/headers";
import { Auth } from "@/app/enums";

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";

export async function fetchApi(endpoint: string, options: RequestInit = {}) {
  const cookieStore = await cookies();
  const token = cookieStore.get(Auth.AUTH_TOKEN)?.value;

  const headers: Record<string, string> = {
    "Content-Type": "application/json",
  };

  // Se options.headers for um objeto (Record<string, string>), fazemos o merge
  if (options.headers && !Array.isArray(options.headers) && !(options.headers instanceof Headers)) {
    Object.assign(headers, options.headers);
  }

  if (token) {
    headers["Authorization"] = `Bearer ${token}`;
  }

  const response = await fetch(`${API_BASE_URL}${endpoint}`, {
    ...options,
    headers,
  });

  return response;
}
