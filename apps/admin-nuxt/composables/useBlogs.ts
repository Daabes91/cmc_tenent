import type { Blog, BlogRequest } from "@/types/blogs";

export function useBlogs() {
  const { fetcher, request } = useAdminApi();

  /**
   * Fetch all blogs (admin view - all statuses)
   */
  async function getAllBlogs(locale?: string) {
    const query = locale ? `?locale=${locale}` : "";
    return await fetcher<Blog[]>(`/blogs${query}`, []);
  }

  /**
   * Get blog by ID
   */
  async function getBlogById(id: number) {
    return await request<Blog>(`/blogs/${id}`);
  }

  /**
   * Get blog by slug
   */
  async function getBlogBySlug(slug: string) {
    return await request<Blog>(`/blogs/slug/${slug}`);
  }

  /**
   * Create a new blog
   */
  async function createBlog(data: BlogRequest) {
    return await request<Blog>(`/blogs`, {
      method: "POST",
      body: data
    });
  }

  /**
   * Update an existing blog
   */
  async function updateBlog(id: number, data: BlogRequest) {
    return await request<Blog>(`/blogs/${id}`, {
      method: "PUT",
      body: data
    });
  }

  /**
   * Delete a blog
   */
  async function deleteBlog(id: number) {
    return await request<void>(`/blogs/${id}`, {
      method: "DELETE"
    });
  }

  /**
   * Publish a blog
   */
  async function publishBlog(id: number) {
    return await request<Blog>(`/blogs/${id}/publish`, {
      method: "PUT"
    });
  }

  /**
   * Unpublish a blog (set to DRAFT)
   */
  async function unpublishBlog(id: number) {
    return await request<Blog>(`/blogs/${id}/unpublish`, {
      method: "PUT"
    });
  }

  return {
    getAllBlogs,
    getBlogById,
    getBlogBySlug,
    createBlog,
    updateBlog,
    deleteBlog,
    publishBlog,
    unpublishBlog
  };
}
