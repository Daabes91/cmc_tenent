package com.clinic.modules.admin.dto;

/**
 * Represents a single search hit across admin modules.
 *
 * @param type     Module identifier (e.g. patients, doctors, appointments).
 * @param id       Identifier of the entity (stringified).
 * @param title    Primary label to display.
 * @param subtitle Secondary label/metadata.
 * @param route    Frontend route to navigate to (optional).
 * @param icon     Suggested icon name for the result (optional).
 */
public record GlobalSearchResultItem(
        String type,
        String id,
        String title,
        String subtitle,
        String route,
        String icon
) {
}
