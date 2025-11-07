<template>
  <div class="relative">
    <select 
      :value="modelValue"
      @input="$emit('update:modelValue', $event.target.value)"
      :class="selectClasses"
      v-bind="$attrs"
    >
      <option 
        v-if="placeholder" 
        :selected="!modelValue" 
        disabled 
        :value="placeholderValue"
      >
        {{ placeholder }}
      </option>
      <slot>
        <option 
          v-for="option in options" 
          :key="getOptionValue(option)" 
          :value="getOptionValue(option)"
        >
          {{ getOptionLabel(option) }}
        </option>
      </slot>
    </select>
    <span class="absolute inset-y-0 end-0 flex items-center pointer-events-none px-3.5">
      <UIcon name="i-heroicons-chevron-down-20-solid" class="flex-shrink-0 text-gray-400 dark:text-gray-500 h-5 w-5" />
    </span>
  </div>
</template>

<script setup lang="ts">
interface Option {
  [key: string]: any;
}

interface Props {
  modelValue?: string | number | null;
  options?: Option[];
  placeholder?: string;
  placeholderValue?: string | number;
  size?: 'sm' | 'md' | 'lg';
  valueAttribute?: string;
  labelAttribute?: string;
  disabled?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  size: 'md',
  valueAttribute: 'value',
  labelAttribute: 'label',
  placeholderValue: '',
  disabled: false
});

defineEmits<{
  'update:modelValue': [value: string | number];
}>();

const selectClasses = computed(() => {
  const baseClasses = 'relative block w-full disabled:cursor-not-allowed disabled:opacity-75 focus:outline-none border-0 form-select rounded-xl shadow-sm bg-white dark:bg-gray-900 ring-1 ring-inset ring-gray-300 dark:ring-gray-700 focus:ring-2 focus:ring-primary-500 dark:focus:ring-primary-400 text-gray-900 dark:text-gray-100';
  
  const sizeClasses = {
    sm: 'text-sm px-2.5 py-1.5 pe-10 h-9',
    md: 'text-sm px-3 py-2 pe-10 h-10',
    lg: 'text-sm px-3.5 py-2.5 pe-12 h-11'
  };
  
  return `${baseClasses} ${sizeClasses[props.size]}`;
});

const getOptionValue = (option: Option) => {
  if (typeof option === 'string' || typeof option === 'number') {
    return option;
  }
  return option[props.valueAttribute];
};

const getOptionLabel = (option: Option) => {
  if (typeof option === 'string' || typeof option === 'number') {
    return option;
  }
  return option[props.labelAttribute];
};
</script>