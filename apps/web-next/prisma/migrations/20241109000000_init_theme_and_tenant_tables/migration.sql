-- CreateTable
CREATE TABLE IF NOT EXISTS "Theme" (
    "id" TEXT NOT NULL,
    "key" TEXT NOT NULL,
    "name" TEXT NOT NULL,
    "status" TEXT NOT NULL DEFAULT 'published',
    "created_at" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT "Theme_pkey" PRIMARY KEY ("id")
);

-- CreateIndex
CREATE UNIQUE INDEX IF NOT EXISTS "Theme_key_key" ON "Theme"("key");

-- CreateIndex
CREATE INDEX IF NOT EXISTS "Theme_status_idx" ON "Theme"("status");

-- AlterTable (only add columns if they don't exist)
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'tenants' AND column_name = 'domain') THEN
        ALTER TABLE "tenants" ADD COLUMN "domain" TEXT;
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'tenants' AND column_name = 'themeId') THEN
        ALTER TABLE "tenants" ADD COLUMN "themeId" TEXT;
    END IF;
END $$;

-- CreateIndex
CREATE UNIQUE INDEX IF NOT EXISTS "tenants_domain_key" ON "tenants"("domain");

-- CreateIndex
CREATE INDEX IF NOT EXISTS "tenants_themeId_idx" ON "tenants"("themeId");

-- AddForeignKey
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'tenants_themeId_fkey'
    ) THEN
        ALTER TABLE "tenants" ADD CONSTRAINT "tenants_themeId_fkey" 
        FOREIGN KEY ("themeId") REFERENCES "Theme"("id") ON DELETE SET NULL ON UPDATE CASCADE;
    END IF;
END $$;
