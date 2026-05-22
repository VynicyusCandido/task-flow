"use client";

import { useState, useEffect } from "react";
import { DragDropContext, DropResult } from "@hello-pangea/dnd";
import { Task, TaskStatus } from "@/@types/Task";
import { Column } from "./Column";
import { moveTask } from "@/app/services/taskService";
import { toast } from "react-toastify";

interface BoardProps {
  projectId: number;
  initialTasks: Task[];
  onTaskClick: (task: Task) => void;
}

export function Board({ projectId, initialTasks, onTaskClick }: BoardProps) {
  const [tasks, setTasks] = useState<Task[]>(initialTasks);

  // Hydration fix for DragDropContext
  const [isMounted, setIsMounted] = useState(false);
  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    setIsMounted(true);
    // eslint-disable-next-line react-hooks/set-state-in-effect
    setTasks(initialTasks);
  }, [initialTasks]);

  if (!isMounted) return <div className="p-8 text-center text-gray-500">Carregando quadro...</div>;

  const onDragEnd = async (result: DropResult) => {
    const { destination, source, draggableId } = result;

    if (!destination) return;

    if (
      destination.droppableId === source.droppableId &&
      destination.index === source.index
    ) {
      return;
    }

    const draggedTask = tasks.find((t) => t.id.toString() === draggableId);
    if (!draggedTask) return;

    const newStatus = destination.droppableId as TaskStatus;
    const previousStatus = source.droppableId as TaskStatus;

    // Optimistic Update
    const newTasks = Array.from(tasks);
    
    // Remove from old position
    const sourceIndex = newTasks.findIndex(t => t.id === draggedTask.id);
    newTasks.splice(sourceIndex, 1);

    // Filter tasks in destination column to find insertion point
    const destColumnTasks = newTasks.filter(t => t.status === newStatus)
      .sort((a, b) => a.orderIndex - b.orderIndex);
    
    // Create updated task
    const updatedTask = { ...draggedTask, status: newStatus };
    
    // Rebuild the array
    // Insert at destination.index within the filtered column
    destColumnTasks.splice(destination.index, 0, updatedTask);
    
    // Re-assign order indexes for destination column
    const reorderedDestTasks = destColumnTasks.map((t, index) => ({
      ...t,
      orderIndex: index
    }));

    // Re-assign order indexes for source column if it's different
    const reorderedSourceTasks = previousStatus === newStatus 
      ? [] 
      : newTasks.filter(t => t.status === previousStatus)
          .sort((a, b) => a.orderIndex - b.orderIndex)
          .map((t, index) => ({ ...t, orderIndex: index }));

    // Merge everything back
    const finalTasks = newTasks
      .filter(t => t.status !== newStatus && t.status !== previousStatus)
      .concat(reorderedDestTasks)
      .concat(reorderedSourceTasks);

    setTasks(finalTasks);

    try {
      const targetOrderIndex = destination.index;
      await moveTask(projectId, draggedTask.id, {
        status: newStatus,
        orderIndex: targetOrderIndex
      });
    } catch (error) {
      console.error("Failed to move task", error);
      toast.error("Erro ao mover a tarefa. As alterações foram revertidas.");
      setTasks(initialTasks); // Revert on failure
    }
  };

  const getTasksByStatus = (status: TaskStatus) => {
    return tasks
      .filter((t) => t.status === status)
      .sort((a, b) => a.orderIndex - b.orderIndex);
  };

  return (
    <DragDropContext onDragEnd={onDragEnd}>
      <div className="flex gap-6 h-full overflow-x-auto pb-4 items-start w-full justify-center">
        <Column
          title="A Fazer"
          status={TaskStatus.TODO}
          tasks={getTasksByStatus(TaskStatus.TODO)}
          onTaskClick={onTaskClick}
        />
        <Column
          title="Em Andamento"
          status={TaskStatus.IN_PROGRESS}
          tasks={getTasksByStatus(TaskStatus.IN_PROGRESS)}
          onTaskClick={onTaskClick}
        />
        <Column
          title="Concluído"
          status={TaskStatus.DONE}
          tasks={getTasksByStatus(TaskStatus.DONE)}
          onTaskClick={onTaskClick}
        />
      </div>
    </DragDropContext>
  );
}
