export interface Project {
  id: number;
  name: string;
  description: string;
  ownerId: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface ProjectMember {
  id: number;
  projectId: number;
  userId: number;
  userName: string;
  userEmail: string;
  role: 'OWNER' | 'MEMBER';
  joinedAt?: string;
}
