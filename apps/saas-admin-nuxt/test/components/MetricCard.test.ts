import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import MetricCard from '~/components/dashboard/MetricCard.vue'

// Mock Nuxt UI components
vi.mock('#app', () => ({
  UCard: {
    name: 'UCard',
    template: '<div class="u-card"><slot /></div>'
  },
  UIcon: {
    name: 'UIcon',
    props: ['name'],
    template: '<span class="u-icon" :data-icon="name"></span>'
  }
}))

describe('MetricCard', () => {
  it('should render with basic props', () => {
    const wrapper = mount(MetricCard, {
      props: {
        title: 'Total Tenants',
        value: 42,
        icon: 'i-heroicons-building-office'
      },
      global: {
        stubs: {
          UCard: { template: '<div class="u-card"><slot /></div>' },
          UIcon: { template: '<span class="u-icon"></span>' },
          LoadingSkeleton: { template: '<div class="skeleton"></div>' }
        }
      }
    })

    expect(wrapper.text()).toContain('Total Tenants')
    expect(wrapper.text()).toContain('42')
  })

  it('should format number values with locale', () => {
    const wrapper = mount(MetricCard, {
      props: {
        title: 'Total Users',
        value: 1234567,
        icon: 'i-heroicons-users'
      },
      global: {
        stubs: {
          UCard: { template: '<div><slot /></div>' },
          UIcon: { template: '<span></span>' },
          LoadingSkeleton: { template: '<div></div>' }
        }
      }
    })

    expect(wrapper.text()).toContain('1,234,567')
  })

  it('should display string values as-is', () => {
    const wrapper = mount(MetricCard, {
      props: {
        title: 'Status',
        value: 'Healthy',
        icon: 'i-heroicons-check-circle'
      },
      global: {
        stubs: {
          UCard: { template: '<div><slot /></div>' },
          UIcon: { template: '<span></span>' },
          LoadingSkeleton: { template: '<div></div>' }
        }
      }
    })

    expect(wrapper.text()).toContain('Healthy')
  })

  it('should display trend indicator when provided', () => {
    const wrapper = mount(MetricCard, {
      props: {
        title: 'Active Users',
        value: 120,
        icon: 'i-heroicons-users',
        trend: {
          value: 15,
          direction: 'up' as const
        }
      },
      global: {
        stubs: {
          UCard: { template: '<div><slot /></div>' },
          UIcon: { template: '<span></span>' },
          LoadingSkeleton: { template: '<div></div>' }
        }
      }
    })

    expect(wrapper.text()).toContain('15%')
  })

  it('should apply correct color class for upward trend', () => {
    const wrapper = mount(MetricCard, {
      props: {
        title: 'Revenue',
        value: 50000,
        icon: 'i-heroicons-currency-dollar',
        trend: {
          value: 10,
          direction: 'up' as const
        }
      },
      global: {
        stubs: {
          UCard: { template: '<div><slot /></div>' },
          UIcon: { template: '<span></span>' },
          LoadingSkeleton: { template: '<div></div>' }
        }
      }
    })

    const trendElement = wrapper.find('.text-green-600')
    expect(trendElement.exists()).toBe(true)
  })

  it('should apply correct color class for downward trend', () => {
    const wrapper = mount(MetricCard, {
      props: {
        title: 'Response Time',
        value: 250,
        icon: 'i-heroicons-clock',
        trend: {
          value: 5,
          direction: 'down' as const
        }
      },
      global: {
        stubs: {
          UCard: { template: '<div><slot /></div>' },
          UIcon: { template: '<span></span>' },
          LoadingSkeleton: { template: '<div></div>' }
        }
      }
    })

    const trendElement = wrapper.find('.text-red-600')
    expect(trendElement.exists()).toBe(true)
  })

  it('should display subtitle when provided', () => {
    const wrapper = mount(MetricCard, {
      props: {
        title: 'Total Tenants',
        value: 42,
        icon: 'i-heroicons-building-office',
        subtitle: 'Last updated 5 minutes ago'
      },
      global: {
        stubs: {
          UCard: { template: '<div><slot /></div>' },
          UIcon: { template: '<span></span>' },
          LoadingSkeleton: { template: '<div></div>' }
        }
      }
    })

    expect(wrapper.text()).toContain('Last updated 5 minutes ago')
  })

  it('should show loading skeleton when loading prop is true', () => {
    const wrapper = mount(MetricCard, {
      props: {
        title: 'Total Tenants',
        value: 42,
        icon: 'i-heroicons-building-office',
        loading: true
      },
      global: {
        stubs: {
          UCard: { template: '<div><slot /></div>' },
          UIcon: { template: '<span></span>' },
          LoadingSkeleton: { template: '<div class="skeleton"></div>' }
        }
      }
    })

    expect(wrapper.find('.skeleton').exists()).toBe(true)
    expect(wrapper.text()).not.toContain('Total Tenants')
  })
})
