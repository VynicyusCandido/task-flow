"use server";

import { User } from "@/@types/User";
import { fetchApi } from "@/lib/api";

export async function getUsers(): Promise<User[]> {
  try {
    const response = await fetchApi("/api/users/me", {
      method: "GET",
      cache: "no-store" // Para sempre pegar os dados frescos do usuário
    });

    if (!response.ok) {
      console.error("Failed to fetch current user", response.status);
      return [];
    }

    const currentUser: User = await response.json();
    
    // Retornamos como array para manter compatibilidade com o dashboard atual
    return [currentUser];
  } catch (error) {
    console.error("Error fetching user:", error);
    return [];
  }
}

// eslint-disable-next-line @typescript-eslint/no-unused-vars
export async function getUserById(_userId: string): Promise<User | undefined> {
  // A ser implementado futuramente quando houver endpoint de busca por ID
  throw new Error("Not implemented yet");
}

// eslint-disable-next-line @typescript-eslint/no-unused-vars
export async function createUser(_userData: Omit<User, "id">): Promise<User> {
  // A criação de usuários agora ocorre na rota de /register pública
  throw new Error("Not implemented yet");
}

// eslint-disable-next-line @typescript-eslint/no-unused-vars
export async function updateUser(_userId: string, _updates: Partial<Omit<User, "id">>): Promise<User | undefined> {
  // A ser implementado quando houver endpoint de PUT /api/users/{id}
  throw new Error("Not implemented yet");
}
