# Bilingual Doctor Support Implementation

## Overview
This implementation adds Arabic and English support for doctor full name, specialty, and bio fields in the admin-nuxt application.

## Changes Made

### Backend Changes

#### 1. Database Schema (Migration Required)
- **File**: `apps/api/src/main/resources/db/migration/V20241026_001__Add_bilingual_doctor_fields.sql`
- **Changes**:
  - Added `full_name_en`, `full_name_ar` columns
  - Added `specialty_en`, `specialty_ar` columns  
  - Added `bio_en`, `bio_ar` columns
  - Migrated existing data to English columns
  - Dropped old single-language columns

#### 2. Entity Layer
- **File**: `apps/api/src/main/java/com/clinic/modules/core/doctor/DoctorEntity.java`
- **Changes**:
  - Replaced `fullName` with `fullNameEn` and `fullNameAr`
  - Replaced `specialty` with `specialtyEn` and `specialtyAr`
  - Replaced `bio` with `bioEn` and `bioAr`
  - Updated constructor and methods

#### 3. DTO Layer
- **File**: `apps/api/src/main/java/com/clinic/modules/admin/dto/DoctorUpsertRequest.java`
- **Changes**: Updated to accept bilingual fields
- **File**: `apps/api/src/main/java/com/clinic/modules/admin/dto/DoctorAdminResponse.java`
- **Changes**: Updated to return bilingual fields

#### 4. Service Layer
- **File**: `apps/api/src/main/java/com/clinic/modules/admin/service/DoctorAdminService.java`
- **Changes**: Updated to handle bilingual data in create/update operations

### Frontend Changes

#### 1. Type Definitions
- **File**: `apps/admin-nuxt/types/doctors.ts`
- **Changes**: Updated `DoctorAdmin` type to include bilingual fields

#### 2. New Doctor Page
- **File**: `apps/admin-nuxt/pages/doctors/new.vue`
- **Changes**:
  - Added separate EN/AR input fields for name, specialty, and bio
  - Updated form validation (English name required)
  - Updated payload structure

#### 3. Edit Doctor Page
- **File**: `apps/admin-nuxt/pages/doctors/[id].vue`
- **Changes**:
  - Added separate EN/AR input fields for name, specialty, and bio
  - Updated form binding and display logic
  - Updated payload structure

#### 4. Other References Updated
- `apps/admin-nuxt/pages/staff/new.vue` - Doctor selection dropdown
- `apps/admin-nuxt/pages/staff/[id].vue` - Doctor selection dropdown
- `apps/admin-nuxt/pages/appointments/[id].vue` - Doctor selection
- `apps/admin-nuxt/pages/appointments/new.vue` - Doctor selection
- `apps/admin-nuxt/pages/treatment-plans/index.vue` - Doctor selection
- `apps/admin-nuxt/pages/doctors/index.vue` - Specialty filtering

## Form Structure

### New Form Fields
```typescript
{
  fullNameEn: string,     // Required
  fullNameAr: string,     // Optional
  specialtyEn: string,    // Optional
  specialtyAr: string,    // Optional
  bioEn: string,          // Optional
  bioAr: string,          // Optional
  // ... other existing fields
}
```

### UI Features
- English fields on the left, Arabic fields on the right
- Arabic fields have `dir="rtl"` for proper text direction
- Placeholder text in appropriate language
- English name is required, Arabic is optional

## Current Status

### Phase 1: Backward Compatible Implementation ✅
- **Backend**: Modified to accept bilingual API requests but store in existing database schema
- **Frontend**: Updated with bilingual forms and backward compatibility for data display
- **API Contract**: New bilingual fields in request/response, with fallback to existing data

### Next Steps

1. **Run Database Migration**: Execute the migration script to update the database schema
2. **Update Backend**: After migration, update entity to use new bilingual columns
3. **Test Full Bilingual Support**: Verify both EN/AR fields work correctly
4. **Remove Backward Compatibility**: Clean up fallback code once migration is complete
5. **Update Public API**: If needed, update the public-facing doctor API to support bilingual fields

## Current Behavior

- **API Requests**: Accept both `fullNameEn`/`fullNameAr` etc. but currently only store English versions
- **API Responses**: Return bilingual structure with English data mapped to `*En` fields, `*Ar` fields as null
- **Frontend**: Displays bilingual forms, handles both old and new data formats
- **Database**: Still uses original single-language columns until migration is run

## Validation Rules

- `fullNameEn`: Required, max 160 characters
- `fullNameAr`: Optional, max 160 characters
- `specialtyEn`: Optional, max 120 characters
- `specialtyAr`: Optional, max 120 characters
- `bioEn`: Optional, unlimited text
- `bioAr`: Optional, unlimited text

## Display Logic

The application uses fallback logic for displaying doctor information:
- Name: `fullNameEn || fullNameAr`
- Specialty: `specialtyEn || specialtyAr`
- Bio: `bioEn || bioAr`

This ensures that if only one language is provided, it will still display correctly.