import { Task, TaskPriority } from "@/@types/Task";

export function validateTaskCreation(task: Partial<Task>): boolean {
  if (!task.title || task.title.trim() === "") {
    return false;
  }

  if (!task.description || task.description.trim() === "") {
    return false;
  }

  if (!task.priority || !Object.values(TaskPriority).includes(task.priority)) {
    return false;
  }

  if (task.assigneeId === undefined || task.assigneeId === null) {
    return false;
  }

  return true;
}
