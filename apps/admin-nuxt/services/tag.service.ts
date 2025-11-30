import { useAdminApi } from "@/composables/useAdminApi";

export interface Tag {
  id: number;
  name: string;
  color?: string;
}

export const useTagService = () => {
  const { request } = useAdminApi();

  const listTags = async (search?: string): Promise<Tag[]> => {
    const params = search ? { search } : {};
    return request<Tag[]>("/patients/tags", {
      method: "GET",
      params
    });
  };

  const createTag = async (name: string, color?: string): Promise<Tag> => {
    return request<Tag>("/patients/tags", {
      method: "POST",
      body: { name, color }
    });
  };

  return {
    listTags,
    createTag
  };
};
