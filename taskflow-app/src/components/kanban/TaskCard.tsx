"use client";

import { Draggable } from "@hello-pangea/dnd";
import { Task, TaskPriority } from "@/@types/Task";
import { format } from "date-fns";
import { CalendarIcon, Clock, GripVertical } from "lucide-react";
import { cn } from "@/lib/utils";

interface TaskCardProps {
  task: Task;
  index: number;
  onClick: (task: Task) => void;
}

const priorityColors = {
  [TaskPriority.LOW]: "bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200",
  [TaskPriority.MEDIUM]: "bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200",
  [TaskPriority.HIGH]: "bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200",
};

const priorityLabels = {
  [TaskPriority.LOW]: "Baixa",
  [TaskPriority.MEDIUM]: "Média",
  [TaskPriority.HIGH]: "Alta",
};

export function TaskCard({ task, index, onClick }: TaskCardProps) {
  return (
    <Draggable draggableId={task.id.toString()} index={index}>
      {(provided, snapshot) => (
        <div
          ref={provided.innerRef}
          {...provided.draggableProps}
          {...provided.dragHandleProps}
          className={cn(
            "bg-card p-4 rounded-lg shadow-sm border border-border mb-3 group cursor-grab active:cursor-grabbing hover:border-primary/50 transition-colors",
            snapshot.isDragging && "shadow-md ring-2 ring-primary ring-opacity-50"
          )}
          onDoubleClick={() => onClick(task)}
        >
          <div className="flex justify-between items-start mb-2">
            <span
              className={cn(
                "text-xs font-semibold px-2 py-1 rounded-full",
                priorityColors[task.priority]
              )}
            >
              {priorityLabels[task.priority]}
            </span>
            <GripVertical className="w-4 h-4 text-muted-foreground opacity-0 group-hover:opacity-100 transition-opacity" />
          </div>
          
          <h4 className="text-sm font-semibold text-card-foreground mb-1 line-clamp-2">
            {task.title}
          </h4>
          
          {task.description && (
            <p className="text-xs text-muted-foreground mb-3 line-clamp-2">
              {task.description}
            </p>
          )}
          
          <div className="flex items-center justify-between text-xs text-muted-foreground mt-2">
            {task.dueDate ? (
              <div className="flex items-center gap-1">
                <CalendarIcon className="w-3 h-3" />
                <span>{format(new Date(task.dueDate), "dd/MM/yyyy")}</span>
              </div>
            ) : (
              <div />
            )}
            
            {task.assigneeName && (
              <div className="w-6 h-6 rounded-full bg-primary text-white flex items-center justify-center font-bold text-[10px]" title={task.assigneeName}>
                {task.assigneeName.charAt(0).toUpperCase()}
              </div>
            )}
          </div>
        </div>
      )}
    </Draggable>
  );
}
