"use server";

import { Task, TaskMoveRequest } from "@/@types/Task";
import { fetchApi } from "@/lib/api";
import { validateTaskCreation } from "@/lib/validations/taskValidation";

export async function getTasks(projectId: number): Promise<Task[]> {
  try {
    const response = await fetchApi(`/api/projects/${projectId}/tasks`, {
      method: "GET",
      cache: "no-store",
    });

    if (!response.ok) {
      console.error("Failed to fetch tasks", response.status);
      return [];
    }

    return await response.json();
  } catch (error) {
    console.error("Error fetching tasks:", error);
    return [];
  }
}

export async function createTask(projectId: number, taskData: Partial<Task>): Promise<Task | null> {
  if (!validateTaskCreation(taskData)) {
    console.error("Task validation failed", taskData);
    return null;
  }

  try {
    const response = await fetchApi(`/api/projects/${projectId}/tasks`, {
      method: "POST",
      body: JSON.stringify(taskData),
    });

    if (!response.ok) return null;
    return await response.json();
  } catch (error) {
    console.error("Error creating task:", error);
    return null;
  }
}

export async function updateTask(projectId: number, taskId: number, task: Partial<Task>): Promise<Task | null> {
  try {
    const response = await fetchApi(`/api/projects/${projectId}/tasks/${taskId}`, {
      method: "PUT",
      body: JSON.stringify(task),
    });

    if (!response.ok) return null;
    return await response.json();
  } catch (error) {
    console.error("Error updating task:", error);
    return null;
  }
}

export async function moveTask(projectId: number, taskId: number, request: TaskMoveRequest): Promise<Task | null> {
  try {
    const response = await fetchApi(`/api/projects/${projectId}/tasks/${taskId}/move`, {
      method: "PATCH",
      body: JSON.stringify(request),
    });

    if (!response.ok) return null;
    return await response.json();
  } catch (error) {
    console.error("Error moving task:", error);
    return null;
  }
}

export async function deleteTask(projectId: number, taskId: number): Promise<boolean> {
  try {
    const response = await fetchApi(`/api/projects/${projectId}/tasks/${taskId}`, {
      method: "DELETE",
    });
    return response.ok;
  } catch (error) {
    console.error("Error deleting task:", error);
    return false;
  }
}
