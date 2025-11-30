import { config } from 'dotenv'
import { Pool } from 'pg'

config({ path: '.env.local' })

const pool = new Pool({
  connectionString: process.env.DATABASE_URL,
})

async function main() {
  console.log('Testing database connection...')
  console.log('DATABASE_URL:', process.env.DATABASE_URL)
  
  try {
    const client = await pool.connect()
    console.log('Connected successfully!')
    
    const result = await client.query('SELECT current_database(), current_user')
    console.log('Query result:', result.rows)
    
    client.release()
    await pool.end()
  } catch (error) {
    console.error('Connection failed:', error)
    process.exit(1)
  }
}

main()
