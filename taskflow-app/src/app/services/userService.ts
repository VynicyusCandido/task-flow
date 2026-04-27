"use server";

import { User } from "@/@types/User";
import { mockUsers } from "@/mocks/users";

export async function getUsers(): Promise<User[]> {
  await new Promise(resolve => setTimeout(resolve, 500));
  return mockUsers;
}

export async function getUserById(userId: string): Promise<User | undefined> {
  await new Promise(resolve => setTimeout(resolve, 500));
  return mockUsers.find(u => u.id === userId);
}

export async function createUser(userData: Omit<User, "id">): Promise<User> {
  await new Promise(resolve => setTimeout(resolve, 500));
  const newUser: User = { ...userData, id: `u${mockUsers.length + 1}` };
  mockUsers.push(newUser);
  return newUser;
}

export async function updateUser(userId: string, updates: Partial<Omit<User, "id">>): Promise<User | undefined> {
  await new Promise(resolve => setTimeout(resolve, 500));
  const index = mockUsers.findIndex(u => u.id === userId);
  if (index === -1) return undefined;
  
  const updatedUser = { ...mockUsers[index], ...updates };
  mockUsers[index] = updatedUser;
  return updatedUser;
}
