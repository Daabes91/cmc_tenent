package com.clinic.modules.admin.staff.model;

/**
 * Represents the different modules in the system that can have permissions.
 */
public enum ModuleName {
    APPOINTMENTS("appointments"),
    PATIENTS("patients"),
    DOCTORS("doctors"),
    SERVICES("services"),
    TREATMENT_PLANS("treatment_plans"),
    REPORTS("reports"),
    SETTINGS("settings"),
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
