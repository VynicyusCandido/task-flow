"use client";

import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card";
import { LayoutDashboard, Lock, Mail } from "lucide-react";
import { authenticateServerAction } from "@/app/services/auth";

export default function LoginPage() {
  const [loading, setLoading] = useState(false);

  const handleLogin = async (formData: FormData) => {
    setLoading(true);
    await authenticateServerAction(formData);
    // Em caso de erro na server action, o loading acaba. Se sucesso, o server faz o redirect.
    setLoading(false);
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-muted/20">
      <Card className="w-full max-w-md shadow border">
        <CardHeader className="space-y-1 text-center">
          <CardTitle className="text-2xl font-bold">TaskFlow</CardTitle>
          <CardDescription>
            Acesse seu workspace
          </CardDescription>
        </CardHeader>
        
        <form action={handleLogin}>
          <CardContent className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="email">Email Corporativo</Label>
              <div className="relative">
                <div className="absolute left-3 top-2.5 text-muted-foreground">
                  <Mail className="h-4 w-4" />
                </div>
                <Input 
                  id="email" 
                  name="email"
                  type="email" 
                  placeholder="voce@empresa.com" 
                  className="pl-9 h-11 bg-background/50" 
                  disabled={loading}
                  required 
                />
              </div>
            </div>
            <div className="space-y-2">
              <div className="flex items-center justify-between">
                <Label htmlFor="password">Senha</Label>
                <a href="#" className="text-sm font-medium text-primary hover:underline">Esqueceu a senha?</a>
              </div>
              <div className="relative">
                <div className="absolute left-3 top-2.5 text-muted-foreground">
                  <Lock className="h-4 w-4" />
                </div>
                <Input 
                  id="password" 
                  name="password"
                  type="password" 
                  className="pl-9 h-11 bg-background/50"
                  disabled={loading}
                  required 
                />
              </div>
            </div>
          </CardContent>
          <CardFooter>
            <Button className="w-full" type="submit" disabled={loading}>
              {loading ? "Entrando..." : "Acessar Workspace"}
            </Button>
          </CardFooter>
        </form>
      </Card>
    </div>
  );
}
