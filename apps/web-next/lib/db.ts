import { Pool, QueryResult } from 'pg'

const connectionString = process.env.DATABASE_URL

if (!connectionString) {
  throw new Error('DATABASE_URL environment variable is required for database access')
}

const pool = new Pool({
  connectionString,
})

export async function dbQuery<T>(
  text: string,
  params: Array<string | number | null> = []
): Promise<QueryResult<T>> {
  return pool.query<T>(text, params)
}
