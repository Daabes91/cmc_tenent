package com.clinic.modules.core.treatment;

/**
 * Defines how frequently follow-up visits should be scheduled automatically.
 */
public enum FollowUpCadence {
    WEEKLY,
    MONTHLY;

    public static FollowUpCadence defaultCadence() {
        return WEEKLY;
    }
}
