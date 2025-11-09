import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import TenantForm from '~/components/tenants/TenantForm.vue'

// Mock i18n
const mockT = vi.fn((key: string, params?: any) => {
  const translations: Record<string, string> = {
    'tenants.form.slug': 'Slug',
    'tenants.form.slugHelp': 'Unique identifier',
    'tenants.form.slugPlaceholder': 'Enter slug',
    'tenants.form.name': 'Name',
    'tenants.form.nameHelp': 'Clinic name',
    'tenants.form.namePlaceholder': 'Enter name',
    'tenants.form.customDomain': 'Custom Domain',
    'tenants.form.customDomainHelp': 'Optional custom domain',
    'tenants.form.customDomainPlaceholder': 'example.com',
    'tenants.form.createButton': 'Create Tenant',
    'tenants.form.saveButton': 'Save Changes',
    'common.cancel': 'Cancel',
    'tenants.form.slugRequired': 'Slug is required',
    'tenants.form.slugMinLength': 'Slug must be at least 3 characters',
    'tenants.form.slugInvalid': 'Slug contains invalid characters',
    'tenants.form.nameRequired': 'Name is required',
    'tenants.form.adminEmailPreview': 'Admin Email Preview',
    'tenants.form.adminEmailDescription': `Admin email will be: ${params?.email || ''}`
  }
  return translations[key] || key
})

vi.mock('#app', () => ({
  useI18n: () => ({
    t: mockT
  })
}))

vi.mock('~/composables/useSaasApi', () => ({
  useSaasApi: () => ({
    getTenants: vi.fn().mockResolvedValue({ content: [] })
  })
}))

describe('TenantForm', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('should render in create mode', () => {
    const wrapper = mount(TenantForm, {
      props: {
        mode: 'create'
      },
      global: {
        stubs: {
          UFormGroup: { template: '<div><label><slot name="label" /></label><slot /></div>' },
          UInput: { template: '<input />' },
          UButton: { template: '<button><slot /></button>' },
          UAlert: { template: '<div class="alert"><slot /></div>' }
        }
      }
    })

    expect(wrapper.text()).toContain('Slug')
    expect(wrapper.text()).toContain('Name')
    expect(wrapper.text()).toContain('Custom Domain')
    expect(wrapper.text()).toContain('Create Tenant')
  })

  it('should render in edit mode', () => {
    const wrapper = mount(TenantForm, {
      props: {
        mode: 'edit',
        initialData: {
          slug: 'existing-clinic',
          name: 'Existing Clinic',
          customDomain: 'existing.com'
        }
      },
      global: {
        stubs: {
          UFormGroup: { template: '<div><slot /></div>' },
          UInput: {
            props: ['modelValue', 'disabled'],
            template: '<input :value="modelValue" :disabled="disabled" />'
          },
          UButton: { template: '<button><slot /></button>' },
          UAlert: { template: '<div><slot /></div>' }
        }
      }
    })

    expect(wrapper.text()).toContain('Save Changes')
  })

  it('should validate slug format', async () => {
    const wrapper = mount(TenantForm, {
      props: {
        mode: 'create'
      },
      global: {
        stubs: {
          UFormGroup: { template: '<div><slot /></div>' },
          UInput: {
            props: ['modelValue'],
            emits: ['input', 'blur'],
            template: '<input :value="modelValue" @input="$emit(\'input\', $event)" @blur="$emit(\'blur\')" />'
          },
          UButton: { template: '<button><slot /></button>' },
          UAlert: { template: '<div><slot /></div>' }
        }
      }
    })

    const slugInput = wrapper.find('input')
    
    // Test invalid slug with uppercase
    await slugInput.setValue('INVALID')
    await slugInput.trigger('blur')
    
    // The component should auto-format to lowercase
    expect(wrapper.vm.formData.slug).toBe('invalid')
  })

  it('should emit submit event with form data', async () => {
    const wrapper = mount(TenantForm, {
      props: {
        mode: 'create'
      },
      global: {
        stubs: {
          UFormGroup: { template: '<div><slot /></div>' },
          UInput: {
            props: ['modelValue'],
            template: '<input :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" />'
          },
          UButton: {
            props: ['disabled', 'loading'],
            template: '<button :disabled="disabled" @click="$emit(\'click\')"><slot /></button>'
          },
          UAlert: { template: '<div><slot /></div>' }
        }
      }
    })

    // Set form data
    wrapper.vm.formData.slug = 'test-clinic'
    wrapper.vm.formData.name = 'Test Clinic'
    wrapper.vm.formData.customDomain = 'test.com'
    wrapper.vm.slugValidation.isValid = true

    // Find and click submit button
    const buttons = wrapper.findAll('button')
    const submitButton = buttons[buttons.length - 1]
    await submitButton.trigger('click')

    expect(wrapper.emitted('submit')).toBeTruthy()
    expect(wrapper.emitted('submit')?.[0]).toEqual([{
      slug: 'test-clinic',
      name: 'Test Clinic',
      customDomain: 'test.com'
    }])
  })

  it('should emit cancel event', async () => {
    const wrapper = mount(TenantForm, {
      props: {
        mode: 'create'
      },
      global: {
        stubs: {
          UFormGroup: { template: '<div><slot /></div>' },
          UInput: { template: '<input />' },
          UButton: {
            template: '<button @click="$emit(\'click\')"><slot /></button>'
          },
          UAlert: { template: '<div><slot /></div>' }
        }
      }
    })

    const buttons = wrapper.findAll('button')
    const cancelButton = buttons[0]
    await cancelButton.trigger('click')

    expect(wrapper.emitted('cancel')).toBeTruthy()
  })

  it('should show admin email preview in create mode', () => {
    const wrapper = mount(TenantForm, {
      props: {
        mode: 'create'
      },
      global: {
        stubs: {
          UFormGroup: { template: '<div><slot /></div>' },
          UInput: {
            props: ['modelValue'],
            template: '<input :value="modelValue" />'
          },
          UButton: { template: '<button><slot /></button>' },
          UAlert: {
            props: ['description'],
            template: '<div class="alert">{{ description }}</div>'
          }
        }
      }
    })

    wrapper.vm.formData.slug = 'my-clinic'
    
    // Wait for reactivity
    wrapper.vm.$nextTick(() => {
      expect(wrapper.vm.adminEmailPreview).toBe('admin@my-clinic.clinic.com')
    })
  })

  it('should disable slug input in edit mode', () => {
    const wrapper = mount(TenantForm, {
      props: {
        mode: 'edit',
        initialData: {
          slug: 'existing-clinic',
          name: 'Existing Clinic'
        }
      },
      global: {
        stubs: {
          UFormGroup: { template: '<div><slot /></div>' },
          UInput: {
            props: ['disabled'],
            template: '<input :disabled="disabled" />'
          },
          UButton: { template: '<button><slot /></button>' },
          UAlert: { template: '<div><slot /></div>' }
        }
      }
    })

    const slugInput = wrapper.find('input')
    expect(slugInput.attributes('disabled')).toBeDefined()
  })

  it('should validate name field', async () => {
    const wrapper = mount(TenantForm, {
      props: {
        mode: 'create'
      },
      global: {
        stubs: {
          UFormGroup: { template: '<div><slot /></div>' },
          UInput: { template: '<input />' },
          UButton: { template: '<button><slot /></button>' },
          UAlert: { template: '<div><slot /></div>' }
        }
      }
    })

    // Test empty name
    wrapper.vm.formData.name = ''
    const isValid = wrapper.vm.validateName()
    
    expect(isValid).toBe(false)
    expect(wrapper.vm.errors.name).toBeTruthy()
  })

  it('should validate custom domain format', () => {
    const wrapper = mount(TenantForm, {
      props: {
        mode: 'create'
      },
      global: {
        stubs: {
          UFormGroup: { template: '<div><slot /></div>' },
          UInput: { template: '<input />' },
          UButton: { template: '<button><slot /></button>' },
          UAlert: { template: '<div><slot /></div>' }
        }
      }
    })

    // Test invalid domain
    wrapper.vm.formData.customDomain = 'invalid domain with spaces'
    const isValid = wrapper.vm.validateCustomDomain()
    
    expect(isValid).toBe(false)
    expect(wrapper.vm.errors.customDomain).toBeTruthy()
  })

  it('should disable submit button when form is invalid', () => {
    const wrapper = mount(TenantForm, {
      props: {
        mode: 'create'
      },
      global: {
        stubs: {
          UFormGroup: { template: '<div><slot /></div>' },
          UInput: { template: '<input />' },
          UButton: {
            props: ['disabled'],
            template: '<button :disabled="disabled"><slot /></button>'
          },
          UAlert: { template: '<div><slot /></div>' }
        }
      }
    })

    const buttons = wrapper.findAll('button')
    const submitButton = buttons[buttons.length - 1]
    
    expect(submitButton.attributes('disabled')).toBeDefined()
  })
})
