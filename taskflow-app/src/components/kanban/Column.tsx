"use client";

import { Droppable } from "@hello-pangea/dnd";
import { Task, TaskStatus } from "@/@types/Task";
import { TaskCard } from "./TaskCard";
import { cn } from "@/lib/utils";

interface ColumnProps {
  status: TaskStatus;
  title: string;
  tasks: Task[];
  onTaskClick: (task: Task) => void;
}

export function Column({ status, title, tasks, onTaskClick }: ColumnProps) {
  return (
    <div className="flex flex-col bg-muted/30 rounded-xl w-80 shrink-0 border border-border max-h-full">
      <div className="p-4 border-b border-border flex items-center justify-between bg-card rounded-t-xl">
        <h3 className="font-semibold text-foreground">{title}</h3>
        <span className="bg-muted text-muted-foreground text-xs font-medium px-2 py-1 rounded-full">
          {tasks.length}
        </span>
      </div>
      
      <Droppable droppableId={status}>
        {(provided, snapshot) => (
          <div
            ref={provided.innerRef}
            {...provided.droppableProps}
            className={cn(
              "p-3 flex-1 overflow-y-auto min-h-[150px] transition-colors",
              snapshot.isDraggingOver ? "bg-muted/80" : ""
            )}
          >
            {tasks.map((task, index) => (
              <TaskCard 
                key={task.id} 
                task={task} 
                index={index} 
                onClick={onTaskClick}
              />
            ))}
            {provided.placeholder}
          </div>
        )}
      </Droppable>
    </div>
  );
}
