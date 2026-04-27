"use server";

import { cookies } from "next/headers";
import { Auth } from "@/app/enums";
import { redirect } from "next/navigation";

// Exemplo de autenticação fluindo unicamente do lado do Servidor com Server Actions
export async function authenticateServerAction(formData: FormData) {
  // Simulando um tempo de processamento como num fetch de backend real.
  // Em cenário real: const res = await fetch('http://localhost:8080/api/auth'...)
  await new Promise((resolve) => setTimeout(resolve, 1000));

  const email = formData.get("email");
  const password = formData.get("password");

  if (!email || !password) {
    return { error: "Email e Senha são obrigatórios" };
  }

  // Pegando a chave agora como variável secreta do servidor
  const dummyToken = process.env.NEXT_PUBLIC_DUMMY_TOKEN || "";
  
  // Gravando o cookie do Next.js de forma nativa e encriptada (HTTP-only default config behaviour)
  const cookieStore = await cookies();
  cookieStore.set(Auth.AUTH_TOKEN, dummyToken, {
    httpOnly: true,
    secure: process.env.NODE_ENV === "production",
    path: "/",
    sameSite: "strict",
  });

  // O redirect joga o usuário imediatamente e termina a Server Action
  redirect("/dashboard");
}

export async function logoutServerAction() {
  const cookieStore = await cookies();
  cookieStore.delete(Auth.AUTH_TOKEN);
  redirect("/login");
}
