'use client';

import { useState, useRef } from 'react';
import { api } from '@/lib/api';

interface ProfileImageUploadProps {
  currentImageUrl?: string | null;
  onImageUploaded: (imageUrl: string) => void;
  userName?: string;
}

export function ProfileImageUpload({
  currentImageUrl,
  onImageUploaded,
  userName = 'User',
}: ProfileImageUploadProps) {
  const [isUploading, setIsUploading] = useState(false);
  const [uploadError, setUploadError] = useState<string | null>(null);
  const [previewUrl, setPreviewUrl] = useState<string | null>(currentImageUrl || null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const handleFileSelect = async (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (!file) return;

    // Validate file type
    const validTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/webp', 'image/gif'];
    if (!validTypes.includes(file.type)) {
      setUploadError('Please select a valid image file (JPEG, PNG, WebP, or GIF)');
      return;
    }

    // Validate file size (10MB max)
    const maxSize = 10 * 1024 * 1024; // 10MB in bytes
    if (file.size > maxSize) {
      setUploadError('Image size must be less than 10MB');
      return;
    }

    // Create preview
    const reader = new FileReader();
    reader.onloadend = () => {
      setPreviewUrl(reader.result as string);
    };
    reader.readAsDataURL(file);

    // Upload image
    setIsUploading(true);
    setUploadError(null);

    try {
      const response = await api.uploadProfileImage(file);
      onImageUploaded(response.publicUrl);
    } catch (error: any) {
      console.error('Image upload failed:', error);
      setUploadError(error.message || 'Failed to upload image. Please try again.');
      setPreviewUrl(currentImageUrl || null);
    } finally {
      setIsUploading(false);
    }
  };

  const handleClick = () => {
    fileInputRef.current?.click();
  };

  const handleRemove = () => {
    setPreviewUrl(null);
    onImageUploaded('');
    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }
  };

  const initials = userName
    .split(' ')
    .map((n) => n[0])
    .join('')
    .toUpperCase()
    .slice(0, 2);

  return (
    <div className="flex flex-col items-center gap-4">
      {/* Image Preview */}
      <div className="relative group">
        <div className="w-24 h-24 rounded-full overflow-hidden border-4 border-white shadow-lg">
          {previewUrl ? (
            <img
              src={previewUrl}
              alt={userName}
              className="w-full h-full object-cover"
            />
          ) : (
            <div className="w-full h-full bg-gradient-to-br from-blue-600 to-cyan-600 flex items-center justify-center">
              <span className="text-xl font-bold text-white">{initials}</span>
            </div>
          )}
        </div>

        {/* Upload/Remove Overlay */}
        <div className="absolute inset-0 rounded-full bg-black/50 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center">
          {previewUrl ? (
            <button
              onClick={handleRemove}
              disabled={isUploading}
              className="p-2 rounded-full bg-red-500 hover:bg-red-600 text-white transition-colors disabled:opacity-50"
              title="Remove image"
            >
              <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"
                />
              </svg>
            </button>
          ) : null}
        </div>

        {/* Loading Spinner */}
        {isUploading && (
          <div className="absolute inset-0 rounded-full bg-black/50 flex items-center justify-center">
            <div className="animate-spin rounded-full h-8 w-8 border-4 border-white border-t-transparent"></div>
          </div>
        )}

        {/* Camera Icon */}
        <button
          onClick={handleClick}
          disabled={isUploading}
          className="absolute bottom-0 right-0 p-2 rounded-full bg-blue-600 hover:bg-blue-700 text-white shadow-lg transition-colors disabled:opacity-50"
          title="Upload image"
        >
          <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M3 9a2 2 0 012-2h.93a2 2 0 001.664-.89l.812-1.22A2 2 0 0110.07 4h3.86a2 2 0 011.664.89l.812 1.22A2 2 0 0018.07 7H19a2 2 0 012 2v9a2 2 0 01-2 2H5a2 2 0 01-2-2V9z"
            />
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 13a3 3 0 11-6 0 3 3 0 016 0z" />
          </svg>
        </button>
      </div>

      {/* File Input */}
      <input
        ref={fileInputRef}
        type="file"
        accept="image/jpeg,image/jpg,image/png,image/webp,image/gif"
        onChange={handleFileSelect}
        className="hidden"
      />

      {/* Error Message */}
      {uploadError && (
        <div className="text-sm text-red-600 text-center max-w-xs">
          {uploadError}
        </div>
      )}

      {/* Help Text */}
      {!uploadError && (
        <p className="text-xs text-slate-500 text-center max-w-xs">
          Click the camera icon to upload a new photo
          <br />
          (JPEG, PNG, WebP or GIF, max 10MB)
        </p>
      )}
    </div>
  );
}
