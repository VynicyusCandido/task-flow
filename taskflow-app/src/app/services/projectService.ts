"use server";

import { Project, ProjectMember } from "@/@types/Project";
import { fetchApi } from "@/lib/api";

export async function getMyProjects(): Promise<Project[]> {
  try {
    const response = await fetchApi("/api/projects", {
      method: "GET",
      cache: "no-store",
    });

    if (!response.ok) {
      console.error("Failed to fetch projects", response.status);
      return [];
    }

    return await response.json();
  } catch (error) {
    console.error("Error fetching projects:", error);
    return [];
  }
}

export async function getProjectById(id: number): Promise<Project | null> {
  try {
    const response = await fetchApi(`/api/projects/${id}`, {
      method: "GET",
      cache: "no-store",
    });

    if (!response.ok) return null;
    return await response.json();
  } catch (error) {
    console.error("Error fetching project:", error);
    return null;
  }
}

export async function createProject(project: Partial<Project>): Promise<Project | null> {
  try {
    const response = await fetchApi("/api/projects", {
      method: "POST",
      body: JSON.stringify(project),
    });

    if (!response.ok) return null;
    return await response.json();
  } catch (error) {
    console.error("Error creating project:", error);
    return null;
  }
}

export async function getProjectMembers(projectId: number): Promise<ProjectMember[]> {
  try {
    const response = await fetchApi(`/api/projects/${projectId}/members`, {
      method: "GET",
      cache: "no-store",
    });

    if (!response.ok) {
      console.error("Failed to fetch project members", response.status);
      return [];
    }

    return await response.json();
  } catch (error) {
    console.error("Error fetching project members:", error);
    return [];
  }
}

export async function deleteProject(id: number): Promise<boolean> {
  try {
    const response = await fetchApi(`/api/projects/${id}`, {
      method: "DELETE",
    });
    return response.ok;
  } catch (error) {
    console.error("Error deleting project:", error);
    return false;
  }
}
