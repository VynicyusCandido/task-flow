import { validateTaskCreation } from "../src/lib/validations/taskValidation";
import { TaskPriority } from "../src/@types/Task";

describe("taskValidation", () => {
  it("should return true when all fields are valid", () => {
    const validTask = {
      title: "Corrigir bug",
      description: "Corrigir o bug crítico em produção",
      priority: TaskPriority.HIGH,
      assigneeId: 1,
    };
    expect(validateTaskCreation(validTask)).toBe(true);
  });

  it("should return false when title is missing or empty", () => {
    const taskWithoutTitle = {
      description: "Descrição da tarefa",
      priority: TaskPriority.MEDIUM,
      assigneeId: 1,
    };
    expect(validateTaskCreation(taskWithoutTitle)).toBe(false);

    const taskEmptyTitle = {
      ...taskWithoutTitle,
      title: "   ",
    };
    expect(validateTaskCreation(taskEmptyTitle)).toBe(false);
  });

  it("should return false when description is missing or empty", () => {
    const taskWithoutDesc = {
      title: "Corrigir bug",
      priority: TaskPriority.MEDIUM,
      assigneeId: 1,
    };
    expect(validateTaskCreation(taskWithoutDesc)).toBe(false);

    const taskEmptyDesc = {
      ...taskWithoutDesc,
      description: "   ",
    };
    expect(validateTaskCreation(taskEmptyDesc)).toBe(false);
  });

  it("should return false when priority is missing or invalid", () => {
    const taskWithoutPriority = {
      title: "Corrigir bug",
      description: "Descrição da tarefa",
      assigneeId: 1,
    };
    expect(validateTaskCreation(taskWithoutPriority)).toBe(false);

    const taskInvalidPriority = {
      ...taskWithoutPriority,
      priority: "INVALID_PRIORITY" as any,
    };
    expect(validateTaskCreation(taskInvalidPriority)).toBe(false);
  });

  it("should return false when assigneeId is missing", () => {
    const taskWithoutAssignee = {
      title: "Corrigir bug",
      description: "Descrição da tarefa",
      priority: TaskPriority.MEDIUM,
    };
    expect(validateTaskCreation(taskWithoutAssignee)).toBe(false);

    const taskNullAssignee = {
      ...taskWithoutAssignee,
      assigneeId: null,
    };
    expect(validateTaskCreation(taskNullAssignee)).toBe(false);
  });
});

