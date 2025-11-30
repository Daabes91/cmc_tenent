package com.clinic.modules.admin.staff.model;

/**
 * Represents the different modules in the system that can have permissions.
 */
public enum ModuleName {
    APPOINTMENTS("appointments"),
    CALENDAR("calendar"),
    PATIENTS("patients"),
    DOCTORS("doctors"),
    MATERIALS("materials"),
    SERVICES("services"),
    INSURANCE_COMPANIES("insurance_companies"),
    TREATMENT_PLANS("treatment_plans"),
    REPORTS("reports"),
    BILLING("billing"),
    TRANSLATIONS("translations"),
    SETTINGS("settings"),
    CLINIC_SETTINGS("clinic_settings"),
    STAFF("staff"),
    BLOGS("blogs");

    private final String columnPrefix;

    ModuleName(String columnPrefix) {
        this.columnPrefix = columnPrefix;
    }

    public String getColumnPrefix() {
        return columnPrefix;
    }

    public String getColumnName() {
        return columnPrefix + "_permissions";
    }
}
