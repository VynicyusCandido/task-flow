"use client";

import { useState, useEffect } from "react";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter, DialogDescription } from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Task, TaskPriority, TaskStatus } from "@/@types/Task";
import { ProjectMember } from "@/@types/Project";
import { validateTaskCreation } from "@/lib/validations/taskValidation";

interface TaskDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  task?: Task | null;
  onSave: (task: Partial<Task>) => Promise<void>;
  status?: TaskStatus; // Default status for new task
  members?: ProjectMember[];
}

export function TaskDialog({ open, onOpenChange, task, onSave, status = TaskStatus.TODO, members = [] }: TaskDialogProps) {
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [priority, setPriority] = useState<TaskPriority>(TaskPriority.MEDIUM);
  const [dueDate, setDueDate] = useState("");
  const [assigneeId, setAssigneeId] = useState<number | null>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (open) {
      if (task) {
        setTitle(task.title);
        setDescription(task.description || "");
        setPriority(task.priority);
        setDueDate(task.dueDate ? new Date(task.dueDate).toISOString().split('T')[0] : "");
        setAssigneeId(task.assigneeId);
      } else {
        setTitle("");
        setDescription("");
        setPriority(TaskPriority.MEDIUM);
        setDueDate("");
        setAssigneeId(null);
      }
    }
  }, [open, task]);

  const handleSave = async () => {
    if (!title.trim()) return;

    setLoading(true);
    try {
      await onSave({
        id: task?.id,
        title,
        description,
        priority,
        status: task?.status || status,
        dueDate: dueDate ? new Date(dueDate).toISOString() : null,
        assigneeId: assigneeId,
      });
      onOpenChange(false);
    } catch (error) {
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[425px]">
        <DialogHeader>
          <DialogTitle>{task ? "Editar Tarefa" : "Nova Tarefa"}</DialogTitle>
          <DialogDescription>
            {task ? "Edite os detalhes da sua tarefa." : "Crie uma nova tarefa para o seu board."}
          </DialogDescription>
        </DialogHeader>
        
        <div className="grid gap-4 py-4">
          <div className="grid gap-2">
            <Label htmlFor="title">Título</Label>
            <Input 
              id="title" 
              value={title} 
              onChange={(e) => setTitle(e.target.value)} 
              placeholder="Ex: Refatorar API" 
            />
          </div>
          
          <div className="grid gap-2">
            <Label htmlFor="description">Descrição</Label>
            <textarea 
              id="description" 
              className="flex min-h-[80px] w-full rounded-md border border-input bg-transparent px-3 py-2 text-sm shadow-sm placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring disabled:cursor-not-allowed disabled:opacity-50"
              value={description} 
              onChange={(e) => setDescription(e.target.value)} 
              placeholder="Detalhes adicionais..." 
            />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div className="grid gap-2">
              <Label htmlFor="assignee">Para Quem (Responsável)</Label>
              <select 
                id="assignee" 
                className="flex h-9 w-full items-center justify-between whitespace-nowrap rounded-md border border-input bg-transparent px-3 py-2 text-sm shadow-sm ring-offset-background placeholder:text-muted-foreground focus:outline-none focus:ring-1 focus:ring-ring disabled:cursor-not-allowed disabled:opacity-50"
                value={assigneeId || ""} 
                onChange={(e) => setAssigneeId(e.target.value ? Number(e.target.value) : null)}
              >
                <option value="">Nenhum (Em aberto)</option>
                {members.map(m => (
                  <option key={m.userId} value={m.userId}>{m.userName}</option>
                ))}
              </select>
            </div>
            
            <div className="grid gap-2">
              <Label htmlFor="priority">Prioridade</Label>
              <select 
                id="priority" 
                className="flex h-9 w-full items-center justify-between whitespace-nowrap rounded-md border border-input bg-transparent px-3 py-2 text-sm shadow-sm ring-offset-background placeholder:text-muted-foreground focus:outline-none focus:ring-1 focus:ring-ring disabled:cursor-not-allowed disabled:opacity-50"
                value={priority} 
                onChange={(e) => setPriority(e.target.value as TaskPriority)}
              >
                <option value={TaskPriority.LOW}>Baixa</option>
                <option value={TaskPriority.MEDIUM}>Média</option>
                <option value={TaskPriority.HIGH}>Alta</option>
              </select>
            </div>
          </div>

          <div className="grid gap-2">
            <Label htmlFor="dueDate">Prazo (Data de Entrega)</Label>
            <Input 
              id="dueDate" 
              type="date"
              value={dueDate} 
              onChange={(e) => setDueDate(e.target.value)} 
            />
          </div>
        </div>

        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)}>Cancelar</Button>
          <Button 
            onClick={handleSave} 
            disabled={
              loading || 
              !validateTaskCreation({ title, description, priority, assigneeId: assigneeId || null })
            }
          >
            {loading ? "Salvando..." : "Salvar Tarefa"}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
