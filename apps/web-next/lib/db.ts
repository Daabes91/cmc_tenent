import { Pool, QueryResult } from 'pg'

const connectionString = process.env.DATABASE_URL
let pool: Pool | null = null

function getPool(): Pool {
  if (!connectionString) {
    throw new Error('DATABASE_URL environment variable is required for database access')
  }

  if (!pool) {
    pool = new Pool({ connectionString })
  }

  return pool
}

export async function dbQuery<T>(
  text: string,
  params: Array<string | number | null> = []
): Promise<QueryResult<T>> {
  return getPool().query<T>(text, params)
}
