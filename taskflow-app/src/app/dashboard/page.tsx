"use client";

import { useEffect, useState } from "react";
import { Button } from "@/components/ui/button";
import { LayoutDashboard, LogOut } from "lucide-react";
import { logoutServerAction } from "@/app/services/auth";
import { getUsers } from "@/app/services/userService";
import { User } from "@/@types/User";

export default function DashboardPage() {
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(true);

  async function loadData() {
    try {
      setLoading(true);
      const allUsers = await getUsers();
      setUsers(allUsers);
    } catch (error) {
      console.error("Falha ao carregar dashboard", error);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadData();
  }, []);

  const handleLogout = async () => {
    await logoutServerAction();
  };

  return (
    <div className="min-h-screen">
      <header className="h-16 border-b flex items-center justify-between px-6 shrink-0 bg-background">
        <div className="flex items-center gap-3">
          <h1 className="font-bold text-lg">TaskFlow Workspace</h1>
        </div>
        <Button variant="outline" onClick={handleLogout} className="gap-2">
          <LogOut className="w-4 h-4" />
          Sair
        </Button>
      </header>

      <main className="flex-1 overflow-hidden flex flex-col p-6 max-w-screen-2xl mx-auto w-full gap-6">
        <div className="flex flex-col gap-2">
          <h2 className="text-xl font-bold">Bem vindo de volta</h2>
          <p>Este é o núcleo do seu sistema.</p>
        </div>
      </main>
    </div>
  );
}
