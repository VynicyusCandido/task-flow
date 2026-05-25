"use client";

import { useEffect, useState, useCallback } from "react";
import { Button } from "@/components/ui/button";
import { LogOut, Plus, RefreshCcw, Trash2, LayoutDashboard } from "lucide-react";
import { logoutServerAction } from "@/app/services/auth";
import { getMyProjects, createProject, getProjectMembers, deleteProject } from "@/app/services/projectService";
import { getTasks, createTask, updateTask } from "@/app/services/taskService";
import { Project, ProjectMember } from "@/@types/Project";
import { Task } from "@/@types/Task";
import { Board } from "@/components/kanban/Board";
import { TaskDialog } from "@/components/tasks/TaskDialog";
import { ProjectDialog } from "@/components/projects/ProjectDialog";
import { DeleteProjectDialog } from "@/components/projects/DeleteProjectDialog";
import { ThemeToggle } from "@/components/ui/ThemeToggle";
import { toast } from "react-toastify";

export default function DashboardPage() {
  const [projects, setProjects] = useState<Project[]>([]);
  const [currentProject, setCurrentProject] = useState<Project | null>(null);
  const [projectMembers, setProjectMembers] = useState<ProjectMember[]>([]);
  const [tasks, setTasks] = useState<Task[]>([]);
  const [loading, setLoading] = useState(true);

  const [isDialogOpen, setIsDialogOpen] = useState(false);
  const [editingTask, setEditingTask] = useState<Task | null>(null);
  
  const [isProjectDialogOpen, setIsProjectDialogOpen] = useState(false);

  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);
  const [projectToDelete, setProjectToDelete] = useState<Project | null>(null);

  const loadData = useCallback(async () => {
    try {
      setLoading(true);
      const userProjects = await getMyProjects();
      setProjects(userProjects);
      
      if (currentProject) {
        // Reload current project tasks and members
        const [projectTasks, members] = await Promise.all([
          getTasks(currentProject.id),
          getProjectMembers(currentProject.id)
        ]);
        
        setTasks(projectTasks);
        setProjectMembers(members);
      }
    } catch (error) {
      console.error("Falha ao carregar dashboard", error);
      toast.error("Erro ao carregar dados do dashboard");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadData();
  }, [loadData]);

  const handleLogout = async () => {
    await logoutServerAction();
  };

  const handleOpenNewTask = () => {
    setEditingTask(null);
    setIsDialogOpen(true);
  };

  const handleTaskClick = (task: Task) => {
    setEditingTask(task);
    setIsDialogOpen(true);
  };

  const handleSaveTask = async (taskData: Partial<Task>) => {
    if (!currentProject) return;

    try {
      if (taskData.id) {
        const updated = await updateTask(currentProject.id, taskData.id, taskData);
        if (updated) {
          setTasks(tasks.map(t => t.id === updated.id ? updated : t));
          toast.success("Tarefa atualizada com sucesso!");
        }
      } else {
        const created = await createTask(currentProject.id, taskData);
        if (created) {
          setTasks([...tasks, created]);
          toast.success("Tarefa criada com sucesso!");
        }
      }
    } catch (error) {
      console.error("Failed to save task", error);
      toast.error("Erro ao salvar tarefa");
      throw error;
    }
  };

  const handleSaveProject = async (projectData: Partial<Project>) => {
    try {
      const created = await createProject(projectData);
      if (created) {
        setProjects([...projects, created]);
        setCurrentProject(created);
        setTasks([]);
        const members = await getProjectMembers(created.id);
        setProjectMembers(members);
        toast.success("Projeto criado com sucesso!");
      }
    } catch (error) {
      console.error("Failed to create project", error);
      toast.error("Erro ao criar projeto");
      throw error;
    }
  };

  const handleDeleteClick = (e: React.MouseEvent, project: Project) => {
    e.stopPropagation();
    setProjectToDelete(project);
    setIsDeleteDialogOpen(true);
  };

  const confirmDeleteProject = async (projectId: number) => {
    try {
      const success = await deleteProject(projectId);
      if (success) {
        setProjects(projects.filter(p => p.id !== projectId));
        if (currentProject?.id === projectId) {
          setCurrentProject(null);
          setTasks([]);
        }
        toast.success("Projeto apagado com sucesso!");
      } else {
        toast.error("Erro ao apagar projeto");
      }
    } catch (error) {
      console.error("Failed to delete project", error);
      toast.error("Erro ao apagar projeto");
    }
  };

  const handleReturnToDashboard = () => {
    setCurrentProject(null);
    setTasks([]);
  };

  const handleSelectProjectFromGrid = async (project: Project) => {
    setCurrentProject(project);
    setLoading(true);
    try {
      const [newTasks, members] = await Promise.all([
        getTasks(project.id),
        getProjectMembers(project.id)
      ]);
      setTasks(newTasks);
      setProjectMembers(members);
    } catch (error) {
      console.error(error);
      toast.error("Erro ao carregar dados do projeto");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="h-screen flex flex-col bg-background transition-colors duration-300">
      <header className="h-16 border-b border-border flex items-center justify-between px-6 shrink-0 bg-card shadow-sm z-10 transition-colors duration-300">
        <div className="flex items-center gap-6">
          <h1 className="font-bold text-xl text-primary flex items-center gap-2">
            📋 TaskFlow
          </h1>
          {projects.length > 0 && currentProject && (
            <div className="flex items-center gap-2 border-l border-border pl-6">
              <Button variant="ghost" size="icon" onClick={handleReturnToDashboard} title="Retornar ao Dashboard" className="mr-2">
                <LayoutDashboard className="w-4 h-4 text-muted-foreground" />
              </Button>
              <span className="text-sm text-muted-foreground">Projeto Atual:</span>
              <select 
                className="text-sm font-semibold bg-transparent text-foreground border-none focus:ring-0 cursor-pointer"
                value={currentProject?.id || ""}
                onChange={async (e) => {
                  const p = projects.find(proj => proj.id === Number(e.target.value));
                  if (p) {
                    setCurrentProject(p);
                    setLoading(true);
                    const [newTasks, members] = await Promise.all([
                      getTasks(p.id),
                      getProjectMembers(p.id)
                    ]);
                    setTasks(newTasks);
                    setProjectMembers(members);
                    setLoading(false);
                  }
                }}
              >
                {projects.map(p => (
                  <option key={p.id} value={p.id}>{p.name}</option>
                ))}
              </select>
            </div>
          )}
        </div>
        <div className="flex items-center gap-4">
          <Button variant="ghost" size="icon" onClick={loadData} title="Atualizar Board">
            <RefreshCcw className="w-4 h-4 text-muted-foreground" />
          </Button>
          <Button variant="outline" onClick={handleLogout} className="gap-2 text-muted-foreground border-border hover:bg-muted">
            <LogOut className="w-4 h-4" />
            Sair
          </Button>
        </div>
      </header>

      <main className="flex-1 overflow-hidden flex flex-col p-6 max-w-screen-2xl mx-auto w-full gap-6">
        {loading ? (
          <div className="flex-1 flex items-center justify-center">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary"></div>
          </div>
        ) : projects.length === 0 ? (
          <div className="flex-1 flex flex-col items-center justify-center text-center max-w-md mx-auto">
            <div className="w-16 h-16 bg-muted rounded-full flex items-center justify-center mb-4">
              <Plus className="w-8 h-8 text-muted-foreground" />
            </div>
            <h2 className="text-xl font-bold text-foreground mb-2">Nenhum projeto encontrado</h2>
            <p className="text-muted-foreground mb-6">Crie um projeto para começar a organizar suas tarefas.</p>
            <Button onClick={() => setIsProjectDialogOpen(true)}>Criar Novo Projeto</Button>
          </div>
        ) : !currentProject ? (
          <div className="flex-1 flex flex-col gap-6">
            <div className="flex justify-between items-center">
              <div>
                <h2 className="text-2xl font-bold text-foreground">Meus Projetos</h2>
                <p className="text-sm text-muted-foreground">Selecione um projeto para gerenciar suas tarefas</p>
              </div>
              <Button onClick={() => setIsProjectDialogOpen(true)} className="gap-2 shadow-sm shadow-primary/20 hover:shadow-md transition-shadow">
                <Plus className="w-4 h-4" />
                Novo Projeto
              </Button>
            </div>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
              {projects.map(project => (
                <div 
                  key={project.id} 
                  className="bg-card border border-border rounded-xl p-6 cursor-pointer hover:border-primary transition-colors shadow-sm hover:shadow-md relative group"
                  onClick={() => handleSelectProjectFromGrid(project)}
                >
                  <Button 
                    variant="ghost" 
                    size="icon" 
                    className="absolute top-4 right-4 opacity-0 group-hover:opacity-100 transition-opacity text-destructive hover:text-destructive hover:bg-destructive/10"
                    onClick={(e) => handleDeleteClick(e, project)}
                    title="Apagar Projeto"
                  >
                    <Trash2 className="w-4 h-4" />
                  </Button>
                  <h3 className="font-bold text-lg mb-2 pr-8">{project.name}</h3>
                  <p className="text-sm text-muted-foreground line-clamp-2">{project.description || "Sem descrição"}</p>
                </div>
              ))}
            </div>
          </div>
        ) : (
          <>
            <div className="flex justify-between items-center">
              <div>
                <h2 className="text-2xl font-bold text-foreground">{currentProject?.name}</h2>
                <p className="text-sm text-muted-foreground">{currentProject?.description || "Gerencie as tarefas deste projeto"}</p>
              </div>
              <Button onClick={handleOpenNewTask} className="gap-2 shadow-sm shadow-primary/20 hover:shadow-md transition-shadow">
                <Plus className="w-4 h-4" />
                Nova Tarefa
              </Button>
            </div>

            <div className="flex-1 overflow-hidden">
              <Board 
                projectId={currentProject!.id} 
                initialTasks={tasks} 
                onTaskClick={handleTaskClick} 
              />
            </div>
          </>
        )}
      </main>

      <TaskDialog
        open={isDialogOpen}
        onOpenChange={setIsDialogOpen}
        task={editingTask}
        onSave={handleSaveTask}
        members={projectMembers}
      />

      <ProjectDialog
        open={isProjectDialogOpen}
        onOpenChange={setIsProjectDialogOpen}
        onSave={handleSaveProject}
      />

      <DeleteProjectDialog
        open={isDeleteDialogOpen}
        onOpenChange={setIsDeleteDialogOpen}
        project={projectToDelete}
        onConfirm={confirmDeleteProject}
      />

      <ThemeToggle />
    </div>
  );
}
