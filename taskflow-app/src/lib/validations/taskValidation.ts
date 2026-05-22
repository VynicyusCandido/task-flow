import { Task, TaskPriority } from "@/@types/Task";

export function validateTaskCreation(task: Partial<Task>): boolean {
  // Check if title is present
  if (!task.title || task.title.trim() === "") {
    return false;
  }

  // Check if description is present and not empty
  if (!task.description || task.description.trim() === "") {
    return false;
  }

  // Check if priority is valid
  if (!task.priority || !Object.values(TaskPriority).includes(task.priority)) {
    return false;
  }

  // Check if assigned to someone
  if (task.assigneeId === undefined || task.assigneeId === null) {
    return false;
  }

  return true;
}
