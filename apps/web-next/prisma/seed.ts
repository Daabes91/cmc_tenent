import { config } from 'dotenv'
import { PrismaClient } from '@prisma/client'

// Load environment variables from .env.local
config({ path: '.env.local' })

const prisma = new PrismaClient()

async function main() {
  console.log('Starting database seed...')

  // Create themes
  const clinicTheme = await prisma.theme.upsert({
    where: { key: 'clinic' },
    update: {},
    create: {
      key: 'clinic',
      name: 'Clinic Theme',
      status: 'published'
    }
  })
  console.log('Created/Updated clinic theme:', clinicTheme.id)

  const barberTheme = await prisma.theme.upsert({
    where: { key: 'barber' },
    update: {},
    create: {
      key: 'barber',
      name: 'Barber Theme',
      status: 'published'
    }
  })
  console.log('Created/Updated barber theme:', barberTheme.id)

  const defaultTheme = await prisma.theme.upsert({
    where: { key: 'default' },
    update: {},
    create: {
      key: 'default',
      name: 'Default Theme',
      status: 'published'
    }
  })
  console.log('Created/Updated default theme:', defaultTheme.id)

  // Create sample tenants with default theme
  const clinicA = await prisma.tenant.upsert({
    where: { slug: 'clinic-a' },
    update: {
      themeId: defaultTheme.id
    },
    create: {
      slug: 'clinic-a',
      name: 'Clinic A',
      status: 'ACTIVE',
      themeId: defaultTheme.id
    }
  })
  console.log('Created/Updated tenant clinic-a:', clinicA.id)

  const clinicB = await prisma.tenant.upsert({
    where: { slug: 'clinic-b' },
    update: {
      themeId: defaultTheme.id
    },
    create: {
      slug: 'clinic-b',
      name: 'Clinic B',
      status: 'ACTIVE',
      themeId: defaultTheme.id
    }
  })
  console.log('Created/Updated tenant clinic-b:', clinicB.id)

  console.log('Seed data created successfully!')
}

main()
  .catch((e) => {
    console.error('Error seeding database:', e)
    process.exit(1)
  })
  .finally(async () => {
    await prisma.$disconnect()
  })
