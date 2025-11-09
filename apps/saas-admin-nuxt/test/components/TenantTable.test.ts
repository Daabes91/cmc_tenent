import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import TenantTable from '~/components/tenants/TenantTable.vue'

const mockTenants = [
  {
    id: 1,
    slug: 'clinic-one',
    name: 'Clinic One',
    customDomain: 'clinic1.com',
    status: 'ACTIVE',
    createdAt: '2024-01-15T10:00:00Z'
  },
  {
    id: 2,
    slug: 'clinic-two',
    name: 'Clinic Two',
    customDomain: null,
    status: 'INACTIVE',
    createdAt: '2024-02-20T14:30:00Z'
  }
]

describe('TenantTable', () => {
  it('should render tenant list', () => {
    const wrapper = mount(TenantTable, {
      props: {
        tenants: mockTenants,
        loading: false,
        sortBy: 'name',
        sortDirection: 'asc'
      },
      global: {
        stubs: {
          UBadge: { template: '<span class="badge"><slot /></span>' },
          UButton: { template: '<button><slot /></button>' },
          UIcon: { template: '<span></span>' }
        }
      }
    })

    expect(wrapper.text()).toContain('Clinic One')
    expect(wrapper.text()).toContain('Clinic Two')
    expect(wrapper.text()).toContain('clinic-one')
    expect(wrapper.text()).toContain('clinic-two')
  })

  it('should display custom domain or dash', () => {
    const wrapper = mount(TenantTable, {
      props: {
        tenants: mockTenants,
        loading: false,
        sortBy: 'name',
        sortDirection: 'asc'
      },
      global: {
        stubs: {
          UBadge: { template: '<span><slot /></span>' },
          UButton: { template: '<button><slot /></button>' },
          UIcon: { template: '<span></span>' }
        }
      }
    })

    expect(wrapper.text()).toContain('clinic1.com')
    expect(wrapper.text()).toContain('-')
  })

  it('should emit sort event when column header is clicked', async () => {
    const wrapper = mount(TenantTable, {
      props: {
        tenants: mockTenants,
        loading: false,
        sortBy: 'name',
        sortDirection: 'asc'
      },
      global: {
        stubs: {
          UBadge: { template: '<span><slot /></span>' },
          UButton: { template: '<button><slot /></button>' },
          UIcon: { template: '<span></span>' }
        }
      }
    })

    const nameHeader = wrapper.findAll('th')[0]
    await nameHeader.trigger('click')

    expect(wrapper.emitted('sort')).toBeTruthy()
    expect(wrapper.emitted('sort')?.[0]).toEqual(['name'])
  })

  it('should emit tenant-click event when row is clicked', async () => {
    const wrapper = mount(TenantTable, {
      props: {
        tenants: mockTenants,
        loading: false,
        sortBy: 'name',
        sortDirection: 'asc'
      },
      global: {
        stubs: {
          UBadge: { template: '<span><slot /></span>' },
          UButton: { template: '<button><slot /></button>' },
          UIcon: { template: '<span></span>' }
        }
      }
    })

    const firstRow = wrapper.findAll('tbody tr')[0]
    await firstRow.trigger('click')

    expect(wrapper.emitted('tenant-click')).toBeTruthy()
    expect(wrapper.emitted('tenant-click')?.[0]).toEqual([1])
  })

  it('should display correct status badge color', () => {
    const wrapper = mount(TenantTable, {
      props: {
        tenants: mockTenants,
        loading: false,
        sortBy: 'name',
        sortDirection: 'asc'
      },
      global: {
        stubs: {
          UBadge: {
            props: ['color'],
            template: '<span :class="`badge-${color}`"><slot /></span>'
          },
          UButton: { template: '<button><slot /></button>' },
          UIcon: { template: '<span></span>' }
        }
      }
    })

    const badges = wrapper.findAll('[class*="badge-"]')
    expect(badges[0].classes()).toContain('badge-green')
    expect(badges[1].classes()).toContain('badge-gray')
  })

  it('should format dates correctly', () => {
    const wrapper = mount(TenantTable, {
      props: {
        tenants: mockTenants,
        loading: false,
        sortBy: 'name',
        sortDirection: 'asc'
      },
      global: {
        stubs: {
          UBadge: { template: '<span><slot /></span>' },
          UButton: { template: '<button><slot /></button>' },
          UIcon: { template: '<span></span>' }
        }
      }
    })

    // Check that dates are formatted (not raw ISO strings)
    expect(wrapper.text()).not.toContain('2024-01-15T10:00:00Z')
    expect(wrapper.text()).toMatch(/Jan|Feb/)
  })

  it('should show empty state when no tenants', () => {
    const wrapper = mount(TenantTable, {
      props: {
        tenants: [],
        loading: false,
        sortBy: 'name',
        sortDirection: 'asc',
        emptyMessage: 'No tenants found'
      },
      global: {
        stubs: {
          UBadge: { template: '<span><slot /></span>' },
          UButton: { template: '<button><slot /></button>' },
          UIcon: { template: '<span></span>' }
        }
      }
    })

    expect(wrapper.text()).toContain('No tenants found')
  })

  it('should display sort indicator on sorted column', () => {
    const wrapper = mount(TenantTable, {
      props: {
        tenants: mockTenants,
        loading: false,
        sortBy: 'name',
        sortDirection: 'asc'
      },
      global: {
        stubs: {
          UBadge: { template: '<span><slot /></span>' },
          UButton: { template: '<button><slot /></button>' },
          UIcon: {
            props: ['name'],
            template: '<span class="sort-icon" :data-icon="name"></span>'
          }
        }
      }
    })

    const sortIcons = wrapper.findAll('.sort-icon')
    expect(sortIcons.length).toBeGreaterThan(0)
  })

  it('should handle pagination correctly', async () => {
    const wrapper = mount(TenantTable, {
      props: {
        tenants: mockTenants,
        loading: false,
        sortBy: 'createdAt',
        sortDirection: 'desc'
      },
      global: {
        stubs: {
          UBadge: { template: '<span><slot /></span>' },
          UButton: { template: '<button><slot /></button>' },
          UIcon: { template: '<span></span>' }
        }
      }
    })

    // Verify all tenants are rendered
    const rows = wrapper.findAll('tbody tr')
    expect(rows.length).toBe(2)
  })
})
