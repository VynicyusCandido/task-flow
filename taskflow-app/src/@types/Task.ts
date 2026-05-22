export enum TaskStatus {
  TODO = 'TODO',
  IN_PROGRESS = 'IN_PROGRESS',
  DONE = 'DONE'
}

export enum TaskPriority {
  LOW = 'LOW',
  MEDIUM = 'MEDIUM',
  HIGH = 'HIGH'
}

export interface Task {
  id: number;
  title: string;
  description: string;
  status: TaskStatus;
  priority: TaskPriority;
  dueDate: string | null;
  orderIndex: number;
  assigneeId: number | null;
  assigneeName: string | null;
  createdAt?: string;
}

export interface TaskMoveRequest {
  status: TaskStatus;
  orderIndex: number;
}

export interface TaskComment {
  id: number;
  content: string;
  authorId: number;
  authorName: string;
  createdAt: string;
}
