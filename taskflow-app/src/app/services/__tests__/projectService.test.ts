import { inviteMember, removeMember } from "../projectService";
import { fetchApi } from "@/lib/api";

jest.mock("@/lib/api", () => ({
  fetchApi: jest.fn(),
}));

describe("projectService - Collaboration", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe("inviteMember", () => {
    it("should call fetchApi with correct parameters and return the new member", async () => {
      const mockMember = { id: 1, userId: 2, userName: "Test User", userEmail: "test@example.com", role: "MEMBER" };
      (fetchApi as jest.Mock).mockResolvedValueOnce({
        ok: true,
        json: async () => mockMember,
      });

      const result = await inviteMember(100, "test@example.com");

      expect(fetchApi).toHaveBeenCalledWith("/api/projects/100/members", {
        method: "POST",
        body: JSON.stringify({ email: "test@example.com" }),
      });
      expect(result).toEqual(mockMember);
    });

    it("should throw an error if the API response is not ok", async () => {
      (fetchApi as jest.Mock).mockResolvedValueOnce({
        ok: false,
        json: async () => ({ message: "User not found" }),
      });

      await expect(inviteMember(100, "nonexistent@example.com")).rejects.toThrow("User not found");
    });

    it("should throw a validation error if the email is empty", async () => {
      await expect(inviteMember(100, "")).rejects.toThrow("E-mail é obrigatório");
      expect(fetchApi).not.toHaveBeenCalled();
    });
  });

  describe("removeMember", () => {
    it("should call fetchApi with correct parameters and return true on success", async () => {
      (fetchApi as jest.Mock).mockResolvedValueOnce({
        ok: true,
      });

      const result = await removeMember(100, 2);

      expect(fetchApi).toHaveBeenCalledWith("/api/projects/100/members/2", {
        method: "DELETE",
      });
      expect(result).toBe(true);
    });

    it("should return false if the API response is not ok", async () => {
      (fetchApi as jest.Mock).mockResolvedValueOnce({
        ok: false,
      });

      const result = await removeMember(100, 2);

      expect(result).toBe(false);
    });
  });
});
