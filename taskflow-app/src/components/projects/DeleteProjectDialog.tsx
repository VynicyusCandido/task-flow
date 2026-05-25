"use client";

import { useState } from "react";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter, DialogDescription } from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Project } from "@/@types/Project";

interface DeleteProjectDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  project: Project | null;
  onConfirm: (projectId: number) => Promise<void>;
}

export function DeleteProjectDialog({ open, onOpenChange, project, onConfirm }: DeleteProjectDialogProps) {
  const [confirmName, setConfirmName] = useState("");
  const [loading, setLoading] = useState(false);

  if (!project) return null;

  const isNameMatching = confirmName === project.name;

  const handleConfirm = async () => {
    if (!isNameMatching) return;

    setLoading(true);
    try {
      await onConfirm(project.id);
      setConfirmName("");
      onOpenChange(false);
    } catch (error) {
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  const handleOpenChange = (isOpen: boolean) => {
    if (!isOpen) {
      setConfirmName("");
    }
    onOpenChange(isOpen);
  };

  return (
    <Dialog open={open} onOpenChange={handleOpenChange}>
      <DialogContent className="sm:max-w-[425px]">
        <DialogHeader>
          <DialogTitle className="text-destructive">Apagar Projeto</DialogTitle>
          <DialogDescription>
            Esta ação <strong>não pode</strong> ser desfeita. Isso irá apagar permanentemente o projeto <strong>{project.name}</strong>, tarefas e membros associados.
          </DialogDescription>
        </DialogHeader>
        
        <div className="grid gap-4 py-4">
          <div className="grid gap-2">
            <Label htmlFor="confirm-name">
              Para confirmar, digite <strong>{project.name}</strong> no campo abaixo:
            </Label>
            <Input 
              id="confirm-name" 
              value={confirmName} 
              onChange={(e) => setConfirmName(e.target.value)} 
              placeholder={project.name}
              autoFocus
            />
          </div>
        </div>

        <DialogFooter>
          <Button variant="outline" onClick={() => handleOpenChange(false)} disabled={loading}>
            Cancelar
          </Button>
          <Button 
            variant="destructive" 
            onClick={handleConfirm} 
            disabled={loading || !isNameMatching}
          >
            {loading ? "Apagando..." : "Apagar este projeto"}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
