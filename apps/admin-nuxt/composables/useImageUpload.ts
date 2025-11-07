import { useAdminApi } from "./useAdminApi";

export interface ImageUploadResponse {
  imageId: string;
  filename: string;
  publicUrl: string;
  variants: Record<string, string>;
}

export interface ImageUploadOptions {
  metadata?: string;
  requireSignedUrls?: boolean;
}

export function useImageUpload() {
  const { request } = useAdminApi();
  const toast = useToast();

  const uploading = ref(false);
  const uploadProgress = ref(0);

  const uploadImage = async (
    file: File, 
    options: ImageUploadOptions = {}
  ): Promise<ImageUploadResponse> => {
    if (!file.type.startsWith('image/')) {
      throw new Error('Please select an image file');
    }

    const maxSize = 10 * 1024 * 1024; // 10MB
    if (file.size > maxSize) {
      throw new Error('File size must be less than 10MB');
    }

    uploading.value = true;
    uploadProgress.value = 0;

    try {
      const formData = new FormData();
      formData.append('file', file);
      
      if (options.metadata) {
        formData.append('metadata', options.metadata);
      }
      
      if (options.requireSignedUrls !== undefined) {
        formData.append('requireSignedUrls', String(options.requireSignedUrls));
      }

      // Simulate progress
      const progressInterval = setInterval(() => {
        if (uploadProgress.value < 90) {
          uploadProgress.value += 10;
        }
      }, 200);

      const response = await request<ImageUploadResponse>('/images/upload', {
        method: 'POST',
        body: formData
      });

      clearInterval(progressInterval);
      uploadProgress.value = 100;

      setTimeout(() => {
        uploading.value = false;
        uploadProgress.value = 0;
      }, 500);

      return response;
    } catch (error: any) {
      uploading.value = false;
      uploadProgress.value = 0;
      throw new Error(error.data?.error?.message || error.message || 'Failed to upload image');
    }
  };

  const uploadMultipleImages = async (
    files: File[], 
    options: ImageUploadOptions = {}
  ): Promise<{ results: any[]; successCount: number; failureCount: number }> => {
    if (files.length === 0) {
      throw new Error('No files provided');
    }

    if (files.length > 10) {
      throw new Error('Maximum 10 files allowed per bulk upload');
    }

    uploading.value = true;
    uploadProgress.value = 0;

    try {
      const formData = new FormData();
      
      files.forEach(file => {
        formData.append('files', file);
      });
      
      if (options.metadata) {
        formData.append('metadata', options.metadata);
      }

      // Simulate progress
      const progressInterval = setInterval(() => {
        if (uploadProgress.value < 90) {
          uploadProgress.value += 5;
        }
      }, 300);

      const response = await request<{ results: any[]; successCount: number; failureCount: number }>('/images/bulk-upload', {
        method: 'POST',
        body: formData
      });

      clearInterval(progressInterval);
      uploadProgress.value = 100;

      setTimeout(() => {
        uploading.value = false;
        uploadProgress.value = 0;
      }, 500);

      return response;
    } catch (error: any) {
      uploading.value = false;
      uploadProgress.value = 0;
      throw new Error(error.data?.error?.message || error.message || 'Failed to upload images');
    }
  };

  const deleteImage = async (imageId: string): Promise<void> => {
    try {
      await request(`/images/${imageId}`, {
        method: 'DELETE'
      });

      toast.add({
        title: "Image deleted",
        description: "Image has been deleted successfully",
        color: "green",
        icon: "i-lucide-check-circle"
      });
    } catch (error: any) {
      const errorMessage = error.data?.error?.message || error.message || 'Failed to delete image';
      toast.add({
        title: "Delete failed",
        description: errorMessage,
        color: "red",
        icon: "i-lucide-alert-circle"
      });
      throw new Error(errorMessage);
    }
  };

  const getImageVariants = async (imageId: string): Promise<Record<string, string>> => {
    try {
      const response = await request<Record<string, string>>(`/images/${imageId}/variants`);
      return response;
    } catch (error: any) {
      throw new Error(error.data?.error?.message || error.message || 'Failed to get image variants');
    }
  };

  return {
    uploading: readonly(uploading),
    uploadProgress: readonly(uploadProgress),
    uploadImage,
    uploadMultipleImages,
    deleteImage,
    getImageVariants
  };
}