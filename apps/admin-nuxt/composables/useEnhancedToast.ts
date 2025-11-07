/**
 * Enhanced toast notifications with icons and better messaging
 */

import { computed } from "vue";

import { useRTLToast } from "./useRTLToast";

// Types for CRUD operations
interface CRUDToastOptions {
  entityName: string;
  entityId?: string | number;
  customMessage?: string;
  locale?: string;
}

interface BatchToastOptions {
  count: number;
  entityType: string;
  operation: string;
}

export const useEnhancedToast = () => {
  const rtlToast = useRTLToast();
  const { t } = useI18n();
  const isRTL = computed(() => rtlToast.isRTL.value);
  
  // Helper function to process errors with hierarchy
  const processError = (error: any, fallbackMessage?: string): string => {
    // Priority 1: API-provided error message
    if (error?.data?.message) return error.data.message;
    if (error?.message) return error.message;
    
    // Priority 2: Fallback message
    if (fallbackMessage) return fallbackMessage;
    
    // Priority 3: Generic error
    return t('toasts.common.unexpectedError', 'An unexpected error occurred. Please try again.');
  };

  // Helper function to get RTL-aware toast configuration
  const baseConfig = {
    success: { color: 'green', icon: 'i-lucide-check-circle-2', wrapper: 'notification-success' },
    error: { color: 'red', icon: 'i-lucide-alert-circle', wrapper: 'notification-error' },
    warning: { color: 'amber', icon: 'i-lucide-alert-triangle', wrapper: 'notification-warning' },
    info: { color: 'blue', icon: 'i-lucide-info', wrapper: 'notification-info' }
  } as const;

  const getToastConfig = (type: keyof typeof baseConfig, customIcon?: string) => ({
    color: baseConfig[type].color,
    icon: customIcon || baseConfig[type].icon,
    timeout: 5000,
    ui: {
      wrapper: `notification-wrapper ${baseConfig[type].wrapper} ${isRTL.value ? 'rtl' : 'ltr'}`,
      container: isRTL.value ? 'rtl-container' : 'ltr-container',
      icon: {
        base: "flex-shrink-0 w-6 h-6"
      },
      // Override the notifications container positioning
      notifications: {
        wrapper: `fixed inset-0 flex flex-col ${isRTL.value ? 'items-start' : 'items-end'} justify-start z-[100] pointer-events-none`,
        position: isRTL.value ? 'top-0 left-0' : 'top-0 right-0'
      }
    }
  });

  return {
    // Success notifications (supports both object and string params)
    success: (titleOrOptions: string | { title: string; description?: string }, description?: string) => {
      const options = typeof titleOrOptions === 'string'
        ? { title: titleOrOptions, description }
        : titleOrOptions;

      const config = getToastConfig('success');
      rtlToast.add({
        title: options.title,
        description: options.description,
        ...config
      });
    },

    // Error notifications (supports both object and string params)
    error: (titleOrOptions: string | { title: string; description?: string; error?: any }, descriptionOrError?: string | any) => {
      let options: { title: string; description?: string; error?: any };

      if (typeof titleOrOptions === 'string') {
        options = {
          title: titleOrOptions,
          description: typeof descriptionOrError === 'string' ? descriptionOrError : undefined,
          error: typeof descriptionOrError !== 'string' ? descriptionOrError : undefined
        };
      } else {
        options = titleOrOptions;
      }

      const description = processError(options.error, options.description);
      const config = getToastConfig('error');
      
      rtlToast.add({
        title: options.title,
        description,
        ...config
      });
    },

    // Warning notifications (supports both object and string params)
    warning: (titleOrOptions: string | { title: string; description?: string }, description?: string) => {
      const options = typeof titleOrOptions === 'string'
        ? { title: titleOrOptions, description }
        : titleOrOptions;

      const config = getToastConfig('warning');
      rtlToast.add({
        title: options.title,
        description: options.description,
        ...config
      });
    },

    // Info notifications (supports both object and string params)
    info: (titleOrOptions: string | { title: string; description?: string }, description?: string) => {
      const options = typeof titleOrOptions === 'string'
        ? { title: titleOrOptions, description }
        : titleOrOptions;

      const config = getToastConfig('info');
      rtlToast.add({
        title: options.title,
        description: options.description,
        ...config
      });
    },

    // Loading notifications (manual close)
    loading: (options: { title: string; description?: string }) => {
      return rtlToast.add({
        title: options.title,
        description: options.description,
        color: "gray",
        icon: "i-lucide-loader-2",
        timeout: 0, // Manual close
        ui: {
          wrapper: `notification-wrapper ${isRTL.value ? 'rtl' : 'ltr'}`,
          container: isRTL.value ? 'rtl-container' : 'ltr-container',
          icon: { 
            base: "flex-shrink-0 w-6 h-6 animate-spin",
            color: "text-gray-500 dark:text-gray-400"
          },
          notifications: {
            wrapper: `fixed inset-0 flex flex-col ${isRTL.value ? 'items-start' : 'items-end'} justify-start z-[100] pointer-events-none`,
            position: isRTL.value ? 'top-0 left-0' : 'top-0 right-0'
          }
        }
      });
    },

    // Specific action toasts
    created: (entityName: string) => {
      const config = getToastConfig('success', "i-lucide-check-circle-2");
      const entityDisplayName = t(`toasts.entities.${entityName}`, entityName);
      rtlToast.add({
        title: t('toasts.operations.create.success', `${entityDisplayName} created successfully`, {
          entity: entityDisplayName
        }),
        description: t('toasts.operations.create.description', `The ${entityDisplayName.toLowerCase()} has been added to the system.`, {
          entity: entityDisplayName.toLowerCase()
        }),
        ...config
      });
    },

    updated: (entityName: string) => {
      const config = getToastConfig('success', "i-lucide-check-circle-2");
      const entityDisplayName = t(`toasts.entities.${entityName}`, entityName);
      rtlToast.add({
        title: t('toasts.operations.update.success', `${entityDisplayName} updated successfully`, {
          entity: entityDisplayName
        }),
        description: t('toasts.operations.update.description', 'Your changes have been saved.'),
        ...config
      });
    },

    deleted: (entityName: string) => {
      const config = getToastConfig('success', "i-lucide-trash-2");
      const entityDisplayName = t(`toasts.entities.${entityName}`, entityName);
      rtlToast.add({
        title: t('toasts.operations.delete.success', `${entityDisplayName} deleted successfully`, {
          entity: entityDisplayName
        }),
        description: t('toasts.operations.delete.description', `The ${entityDisplayName.toLowerCase()} has been removed from the system.`, {
          entity: entityDisplayName.toLowerCase()
        }),
        ...config
      });
    },

    saved: () => {
      const config = getToastConfig('success', "i-lucide-save");
      rtlToast.add({
        title: t('toasts.common.saved.title', 'Changes saved'),
        description: t('toasts.common.saved.description', 'Your updates have been successfully applied.'),
        ...config
      });
    },

    cancelled: () => {
      rtlToast.add({
        title: t('toasts.common.actionCancelled.title', 'Action cancelled'),
        description: t('toasts.common.actionCancelled.description', 'No changes were made.'),
        color: "gray",
        icon: "i-lucide-x-circle",
        timeout: 5000,
        ui: {
          wrapper: `notification-wrapper ${isRTL.value ? 'rtl' : 'ltr'}`,
          container: isRTL.value ? 'rtl-container' : 'ltr-container',
          icon: { 
            base: "flex-shrink-0 w-6 h-6",
            color: "text-gray-500 dark:text-gray-400"
          }
        }
      });
    },

    comingSoon: (feature: string) => {
      const config = getToastConfig('info', "i-lucide-sparkles");
      rtlToast.add({
        title: t('toasts.common.comingSoon.title', 'Coming soon'),
        description: t('toasts.common.comingSoon.description', `${feature} will be available in the next update.`, {
          feature
        }),
        ...config
      });
    },

    // New CRUD-specific methods
    operationSuccess: (operation: 'create' | 'update' | 'delete', options: CRUDToastOptions) => {
      const config = getToastConfig('success');
      const entityDisplayName = t(`toasts.entities.${options.entityName}`, options.entityName);
      
      let title: string;
      let description: string;

      if (options.customMessage) {
        title = options.customMessage;
        description = '';
      } else {
        title = t(`toasts.operations.${operation}.success`, `${entityDisplayName} ${operation}d successfully`, {
          entity: entityDisplayName
        });
        
        // Add contextual descriptions
        switch (operation) {
          case 'create':
            description = t(`toasts.operations.create.description`, `The ${entityDisplayName.toLowerCase()} has been added to the system.`, {
              entity: entityDisplayName.toLowerCase()
            });
            break;
          case 'update':
            description = t(`toasts.operations.update.description`, 'Your changes have been saved.');
            break;
          case 'delete':
            description = t(`toasts.operations.delete.description`, `The ${entityDisplayName.toLowerCase()} has been removed from the system.`, {
              entity: entityDisplayName.toLowerCase()
            });
            break;
        }
      }

      rtlToast.add({
        title,
        description,
        ...config
      });
    },

    operationError: (operation: 'create' | 'update' | 'delete', options: CRUDToastOptions & { error?: any }) => {
      const config = getToastConfig('error');
      const entityDisplayName = t(`toasts.entities.${options.entityName}`, options.entityName);
      
      const title = options.customMessage || t(`toasts.operations.${operation}.error`, `Failed to ${operation} ${entityDisplayName.toLowerCase()}`, {
        entity: entityDisplayName.toLowerCase()
      });
      
      const description = processError(options.error, t(`toasts.operations.${operation}.errorDescription`, `Unable to ${operation} the ${entityDisplayName.toLowerCase()}. Please try again.`, {
        entity: entityDisplayName.toLowerCase()
      }));

      rtlToast.add({
        title,
        description,
        ...config
      });
    },

    // Batch operations
    batchSuccess: (operation: string, count: number, entityType: string) => {
      const config = getToastConfig('success');
      const entityDisplayName = t(`toasts.entities.${entityType}`, entityType);
      
      const title = t('toasts.batch.success', `${count} ${entityDisplayName.toLowerCase()}${count > 1 ? 's' : ''} ${operation} successfully`, {
        count,
        entity: entityDisplayName.toLowerCase(),
        operation
      });

      rtlToast.add({
        title,
        description: '',
        ...config
      });
    },

    batchError: (operation: string, errors: any[], entityType: string) => {
      const config = getToastConfig('error');
      const entityDisplayName = t(`toasts.entities.${entityType}`, entityType);
      
      const title = t('toasts.batch.error', `Failed to ${operation} some ${entityDisplayName.toLowerCase()}s`, {
        entity: entityDisplayName.toLowerCase(),
        operation
      });
      
      const description = t('toasts.batch.errorDescription', `${errors.length} operation${errors.length > 1 ? 's' : ''} failed. Please try again.`, {
        count: errors.length
      });

      rtlToast.add({
        title,
        description,
        ...config
      });
    },

    // Enhanced versions of existing methods with localization
    createSuccess: (entityName: string, customMessage?: string) => {
      const config = getToastConfig('success');
      const entityDisplayName = t(`toasts.entities.${entityName}`, entityName);
      
      const title = customMessage || t(`toasts.operations.create.success`, `${entityDisplayName} created successfully`, {
        entity: entityDisplayName
      });
      
      const description = t(`toasts.operations.create.description`, `The ${entityDisplayName.toLowerCase()} has been added to the system.`, {
        entity: entityDisplayName.toLowerCase()
      });

      rtlToast.add({
        title,
        description,
        ...config
      });
    },

    updateSuccess: (entityName: string, customMessage?: string) => {
      const config = getToastConfig('success');
      const entityDisplayName = t(`toasts.entities.${entityName}`, entityName);
      
      const title = customMessage || t(`toasts.operations.update.success`, `${entityDisplayName} updated successfully`, {
        entity: entityDisplayName
      });
      
      const description = t(`toasts.operations.update.description`, 'Your changes have been saved.');

      rtlToast.add({
        title,
        description,
        ...config
      });
    },

    deleteSuccess: (entityName: string, customMessage?: string) => {
      const config = getToastConfig('success');
      const entityDisplayName = t(`toasts.entities.${entityName}`, entityName);
      
      const title = customMessage || t(`toasts.operations.delete.success`, `${entityDisplayName} deleted successfully`, {
        entity: entityDisplayName
      });
      
      const description = t(`toasts.operations.delete.description`, `The ${entityDisplayName.toLowerCase()} has been removed from the system.`, {
        entity: entityDisplayName.toLowerCase()
      });

      rtlToast.add({
        title,
        description,
        ...config
      });
    },

    createError: (entityName: string, error?: any, customMessage?: string) => {
      const config = getToastConfig('error');
      const entityDisplayName = t(`toasts.entities.${entityName}`, entityName);
      
      const title = customMessage || t(`toasts.operations.create.error`, `Failed to create ${entityDisplayName.toLowerCase()}`, {
        entity: entityDisplayName.toLowerCase()
      });
      
      const description = processError(error, t(`toasts.operations.create.errorDescription`, `Unable to create the ${entityDisplayName.toLowerCase()}. Please try again.`, {
        entity: entityDisplayName.toLowerCase()
      }));

      rtlToast.add({
        title,
        description,
        ...config
      });
    },

    updateError: (entityName: string, error?: any, customMessage?: string) => {
      const config = getToastConfig('error');
      const entityDisplayName = t(`toasts.entities.${entityName}`, entityName);
      
      const title = customMessage || t(`toasts.operations.update.error`, `Failed to update ${entityDisplayName.toLowerCase()}`, {
        entity: entityDisplayName.toLowerCase()
      });
      
      const description = processError(error, t(`toasts.operations.update.errorDescription`, `Unable to update the ${entityDisplayName.toLowerCase()}. Please try again.`, {
        entity: entityDisplayName.toLowerCase()
      }));

      rtlToast.add({
        title,
        description,
        ...config
      });
    },

    deleteError: (entityName: string, error?: any, customMessage?: string) => {
      const config = getToastConfig('error');
      const entityDisplayName = t(`toasts.entities.${entityName}`, entityName);
      
      const title = customMessage || t(`toasts.operations.delete.error`, `Failed to delete ${entityDisplayName.toLowerCase()}`, {
        entity: entityDisplayName.toLowerCase()
      });
      
      const description = processError(error, t(`toasts.operations.delete.errorDescription`, `Unable to delete the ${entityDisplayName.toLowerCase()}. Please try again.`, {
        entity: entityDisplayName.toLowerCase()
      }));

      rtlToast.add({
        title,
        description,
        ...config
      });
    }
  };
};
