import { config } from 'dotenv'
import { PrismaClient } from '@prisma/client'

config({ path: '.env.local' })

const prisma = new PrismaClient({
  log: ['query', 'info', 'warn', 'error'],
})

async function main() {
  console.log('Testing database connection...')
  console.log('DATABASE_URL:', process.env.DATABASE_URL)
  
  try {
    await prisma.$connect()
    console.log('Connected successfully!')
    
    const result = await prisma.$queryRaw`SELECT current_database(), current_user`
    console.log('Query result:', result)
    
    await prisma.$disconnect()
  } catch (error) {
    console.error('Connection failed:', error)
    process.exit(1)
  }
}

main()
