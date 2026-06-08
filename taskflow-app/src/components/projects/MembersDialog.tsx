"use client";

import { useState } from "react";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription } from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { ProjectMember } from "@/@types/Project";
import { User, Mail, UserMinus, ShieldCheck } from "lucide-react";
import { inviteMember, removeMember } from "@/app/services/projectService";
import { toast } from "react-toastify";

interface MembersDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  projectId: number;
  members: ProjectMember[];
  onMembersChange: (members: ProjectMember[]) => void;
}

export function MembersDialog({ open, onOpenChange, projectId, members, onMembersChange }: MembersDialogProps) {
  const [email, setEmail] = useState("");
  const [loading, setLoading] = useState(false);

  const handleInvite = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!email.trim()) return;

    setLoading(true);
    try {
      const newMember = await inviteMember(projectId, email);
      if (newMember) {
        onMembersChange([...members, newMember]);
        setEmail("");
        toast.success("Usuário convidado com sucesso!");
      }
    } catch (error: unknown) {
      const message = error instanceof Error ? error.message : "Erro ao convidar usuário";
      toast.error(message);
    } finally {
      setLoading(false);
    }
  };

  const handleRemove = async (userId: number) => {
    try {
      const success = await removeMember(projectId, userId);
      if (success) {
        onMembersChange(members.filter(m => m.userId !== userId));
        toast.success("Membro removido do projeto");
      }
    } catch {
      toast.error("Erro ao remover membro");
    }
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[500px]">
        <DialogHeader>
          <DialogTitle>Membros do Projeto</DialogTitle>
          <DialogDescription>
            Gerencie quem tem acesso a este projeto e convide novos colaboradores.
          </DialogDescription>
        </DialogHeader>

        <div className="space-y-6 py-4">
          <form onSubmit={handleInvite} className="space-y-2">
            <Label htmlFor="email">Convidar via E-mail</Label>
            <div className="flex gap-2">
              <div className="relative flex-1">
                <Mail className="absolute left-3 top-2.5 h-4 w-4 text-muted-foreground" />
                <Input
                  id="email"
                  placeholder="exemplo@empresa.com"
                  className="pl-9"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  type="email"
                  required
                />
              </div>
              <Button type="submit" disabled={loading}>
                {loading ? "Enviando..." : "Convidar"}
              </Button>
            </div>
          </form>

          <div className="space-y-4">
            <Label>Membros Atuais ({members.length})</Label>
            <div className="space-y-3 max-h-[300px] overflow-y-auto pr-2">
              {members.map((member) => (
                <div key={member.userId} className="flex items-center justify-between p-3 rounded-lg border border-border bg-card shadow-sm">
                  <div className="flex items-center gap-3">
                    <div className="w-8 h-8 rounded-full bg-primary/10 flex items-center justify-center text-primary font-bold text-xs">
                      {member.userName.substring(0, 2).toUpperCase()}
                    </div>
                    <div>
                      <div className="text-sm font-medium flex items-center gap-2">
                        {member.userName}
                        {member.role === 'OWNER' ? (
                          <span className="flex items-center gap-1 text-[10px] bg-primary/20 text-primary px-1.5 py-0.5 rounded-full font-bold uppercase">
                            <ShieldCheck className="w-2.5 h-2.5" />
                            Dono
                          </span>
                        ) : (
                          <span className="flex items-center gap-1 text-[10px] bg-muted text-muted-foreground px-1.5 py-0.5 rounded-full font-bold uppercase">
                            <User className="w-2.5 h-2.5" />
                            Membro
                          </span>
                        )}
                      </div>
                      <div className="text-xs text-muted-foreground">{member.userEmail}</div>
                    </div>
                  </div>
                  
                  {member.role !== 'OWNER' && (
                    <Button 
                      variant="ghost" 
                      size="icon" 
                      className="text-destructive hover:text-destructive hover:bg-destructive/10"
                      onClick={() => handleRemove(member.userId)}
                      title="Remover do projeto"
                    >
                      <UserMinus className="w-4 h-4" />
                    </Button>
                  )}
                </div>
              ))}
            </div>
          </div>
        </div>
      </DialogContent>
    </Dialog>
  );
}
