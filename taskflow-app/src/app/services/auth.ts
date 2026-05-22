"use server";

import { cookies } from "next/headers";
import { Auth } from "@/app/enums";
import { redirect } from "next/navigation";

export async function authenticateServerAction(formData: FormData) {
  const email = formData.get("email") as string;
  const password = formData.get("password") as string;

  if (!email || !password) {
    return { error: "Email e Senha são obrigatórios" };
  }

  try {
    const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";
    
    const response = await fetch(`${API_BASE_URL}/api/auth/login`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ email, password }),
    });

    if (!response.ok) {
      return { error: "E-mail ou senha incorretos" };
    }

    const data = await response.json();
    const jwtToken = data.token;

    if (!jwtToken) {
      return { error: "Token não retornado pelo servidor" };
    }

    const cookieStore = await cookies();
    cookieStore.set(Auth.AUTH_TOKEN, jwtToken, {
      httpOnly: true,
      secure: process.env.NODE_ENV === "production",
      path: "/",
      sameSite: "lax",
      maxAge: 60 * 60 * 24,
    });

  } catch (error) {
    console.error("Login failed:", error);
    return { error: "Erro interno no servidor de autenticação" };
  }

  redirect("/dashboard");
}

export async function logoutServerAction() {
  const cookieStore = await cookies();
  cookieStore.delete(Auth.AUTH_TOKEN);
  redirect("/login");
}
