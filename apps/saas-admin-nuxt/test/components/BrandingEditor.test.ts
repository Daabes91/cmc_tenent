import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import BrandingEditor from '~/components/tenants/BrandingEditor.vue'

const mockToastAdd = vi.fn()

vi.mock('#app', () => ({
  useToast: () => ({
    add: mockToastAdd
  }),
  useRuntimeConfig: () => ({
    public: {
      saasApiBase: 'http://localhost:8080/saas'
    }
  })
}))

vi.mock('~/composables/useSaasAuth', () => ({
  useSaasAuth: () => ({
    getToken: () => 'test-token'
  })
}))

const mockBrandingConfig = {
  primaryColor: '#3B82F6',
  secondaryColor: '#10B981',
  logoUrl: null
}

describe('BrandingEditor', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('should render with initial branding config', () => {
    const wrapper = mount(BrandingEditor, {
      props: {
        modelValue: mockBrandingConfig,
        tenantId: 1
      },
      global: {
        stubs: {
          UCard: { template: '<div><slot name="header" /><slot /></div>' },
          UIcon: { template: '<span></span>' },
          UInput: {
            props: ['modelValue'],
            template: '<input :value="modelValue" />'
          },
          UButton: { template: '<button><slot /></button>' }
        }
      }
    })

    expect(wrapper.text()).toContain('Branding Settings')
    expect(wrapper.text()).toContain('Primary Color')
    expect(wrapper.text()).toContain('Secondary Color')
  })

  it('should display color pickers with current values', () => {
    const wrapper = mount(BrandingEditor, {
      props: {
        modelValue: mockBrandingConfig,
        tenantId: 1
      },
      global: {
        stubs: {
          UCard: { template: '<div><slot /></div>' },
          UIcon: { template: '<span></span>' },
          UInput: { template: '<input />' },
          UButton: { template: '<button><slot /></button>' }
        }
      }
    })

    const colorInputs = wrapper.findAll('input[type="color"]')
    expect(colorInputs.length).toBe(2)
    expect(colorInputs[0].element.value).toBe('#3B82F6')
    expect(colorInputs[1].element.value).toBe('#10B981')
  })

  it('should emit update when primary color changes', async () => {
    const wrapper = mount(BrandingEditor, {
      props: {
        modelValue: mockBrandingConfig,
        tenantId: 1
      },
      global: {
        stubs: {
          UCard: { template: '<div><slot /></div>' },
          UIcon: { template: '<span></span>' },
          UInput: {
            props: ['modelValue'],
            emits: ['input'],
            template: '<input :value="modelValue" @input="$emit(\'input\', $event)" />'
          },
          UButton: { template: '<button><slot /></button>' }
        }
      }
    })

    const colorInput = wrapper.findAll('input[type="color"]')[0]
    await colorInput.setValue('#FF0000')
    await colorInput.trigger('input')

    expect(wrapper.emitted('update:modelValue')).toBeTruthy()
    const emittedValue = wrapper.emitted('update:modelValue')?.[0]?.[0] as any
    expect(emittedValue.primaryColor).toBe('#FF0000')
  })

  it('should emit update when secondary color changes', async () => {
    const wrapper = mount(BrandingEditor, {
      props: {
        modelValue: mockBrandingConfig,
        tenantId: 1
      },
      global: {
        stubs: {
          UCard: { template: '<div><slot /></div>' },
          UIcon: { template: '<span></span>' },
          UInput: {
            props: ['modelValue'],
            emits: ['input'],
            template: '<input :value="modelValue" @input="$emit(\'input\', $event)" />'
          },
          UButton: { template: '<button><slot /></button>' }
        }
      }
    })

    const colorInput = wrapper.findAll('input[type="color"]')[1]
    await colorInput.setValue('#00FF00')
    await colorInput.trigger('input')

    expect(wrapper.emitted('update:modelValue')).toBeTruthy()
    const emittedValue = wrapper.emitted('update:modelValue')?.[0]?.[0] as any
    expect(emittedValue.secondaryColor).toBe('#00FF00')
  })

  it('should validate hex color format', () => {
    const wrapper = mount(BrandingEditor, {
      props: {
        modelValue: mockBrandingConfig,
        tenantId: 1
      },
      global: {
        stubs: {
          UCard: { template: '<div><slot /></div>' },
          UIcon: { template: '<span></span>' },
          UInput: { template: '<input />' },
          UButton: { template: '<button><slot /></button>' }
        }
      }
    })

    // Test valid hex colors
    expect(wrapper.vm.validateHexColor('#3B82F6')).toBe(true)
    expect(wrapper.vm.validateHexColor('#FFF')).toBe(true)
    
    // Test invalid hex colors
    expect(wrapper.vm.validateHexColor('3B82F6')).toBe(false)
    expect(wrapper.vm.validateHexColor('#GGGGGG')).toBe(false)
    expect(wrapper.vm.validateHexColor('blue')).toBe(false)
  })

  it('should apply color preset', async () => {
    const wrapper = mount(BrandingEditor, {
      props: {
        modelValue: mockBrandingConfig,
        tenantId: 1
      },
      global: {
        stubs: {
          UCard: { template: '<div><slot /></div>' },
          UIcon: { template: '<span></span>' },
          UInput: { template: '<input />' },
          UButton: { template: '<button><slot /></button>' }
        }
      }
    })

    const preset = { primary: '#8B5CF6', secondary: '#EC4899' }
    wrapper.vm.applyPreset(preset)

    expect(wrapper.emitted('update:modelValue')).toBeTruthy()
    const emittedValue = wrapper.emitted('update:modelValue')?.[0]?.[0] as any
    expect(emittedValue.primaryColor).toBe('#8B5CF6')
    expect(emittedValue.secondaryColor).toBe('#EC4899')
  })

  it('should display logo preview when logoUrl exists', () => {
    const configWithLogo = {
      ...mockBrandingConfig,
      logoUrl: 'https://example.com/logo.png'
    }

    const wrapper = mount(BrandingEditor, {
      props: {
        modelValue: configWithLogo,
        tenantId: 1
      },
      global: {
        stubs: {
          UCard: { template: '<div><slot /></div>' },
          UIcon: { template: '<span></span>' },
          UInput: { template: '<input />' },
          UButton: { template: '<button><slot /></button>' }
        }
      }
    })

    const logoImg = wrapper.find('img[alt="Current Logo"]')
    expect(logoImg.exists()).toBe(true)
    expect(logoImg.attributes('src')).toBe('https://example.com/logo.png')
  })

  it('should remove logo when remove button is clicked', async () => {
    const configWithLogo = {
      ...mockBrandingConfig,
      logoUrl: 'https://example.com/logo.png'
    }

    const wrapper = mount(BrandingEditor, {
      props: {
        modelValue: configWithLogo,
        tenantId: 1
      },
      global: {
        stubs: {
          UCard: { template: '<div><slot /></div>' },
          UIcon: { template: '<span></span>' },
          UInput: { template: '<input />' },
          UButton: { template: '<button><slot /></button>' }
        }
      }
    })

    const removeButton = wrapper.find('button[type="button"]')
    await removeButton.trigger('click')

    expect(wrapper.emitted('update:modelValue')).toBeTruthy()
    const emittedValue = wrapper.emitted('update:modelValue')?.[0]?.[0] as any
    expect(emittedValue.logoUrl).toBe(null)
  })

  it('should show validation errors', () => {
    const wrapper = mount(BrandingEditor, {
      props: {
        modelValue: mockBrandingConfig,
        tenantId: 1,
        validationErrors: {
          primaryColor: 'Invalid color format'
        }
      },
      global: {
        stubs: {
          UCard: { template: '<div><slot /></div>' },
          UIcon: { template: '<span></span>' },
          UInput: {
            props: ['error'],
            template: '<input :class="{ error: error }" />'
          },
          UButton: { template: '<button><slot /></button>' }
        }
      }
    })

    expect(wrapper.text()).toContain('Invalid color format')
  })

  it('should display color presets', () => {
    const wrapper = mount(BrandingEditor, {
      props: {
        modelValue: mockBrandingConfig,
        tenantId: 1
      },
      global: {
        stubs: {
          UCard: { template: '<div><slot /></div>' },
          UIcon: { template: '<span></span>' },
          UInput: { template: '<input />' },
          UButton: { template: '<button><slot /></button>' }
        }
      }
    })

    expect(wrapper.text()).toContain('Color Presets')
    const presetButtons = wrapper.findAll('button[type="button"]').filter(btn => 
      btn.attributes('title')?.includes('&')
    )
    expect(presetButtons.length).toBeGreaterThan(0)
  })

  it('should update logo URL from text input', async () => {
    const wrapper = mount(BrandingEditor, {
      props: {
        modelValue: mockBrandingConfig,
        tenantId: 1
      },
      global: {
        stubs: {
          UCard: { template: '<div><slot /></div>' },
          UIcon: { template: '<span></span>' },
          UInput: {
            props: ['modelValue', 'placeholder'],
            emits: ['blur'],
            template: '<input :value="modelValue" :placeholder="placeholder" @blur="$emit(\'blur\')" />'
          },
          UButton: { template: '<button><slot /></button>' }
        }
      }
    })

    wrapper.vm.logoUrlInput = 'https://example.com/new-logo.png'
    wrapper.vm.handleLogoUrlChange()

    expect(wrapper.emitted('update:modelValue')).toBeTruthy()
    const emittedValue = wrapper.emitted('update:modelValue')?.[0]?.[0] as any
    expect(emittedValue.logoUrl).toBe('https://example.com/new-logo.png')
  })
})
