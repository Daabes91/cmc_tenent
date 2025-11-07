package com.clinic.modules.core.email;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Utility class for generating iCalendar (.ics) files for appointment reminders.
 * Generates RFC 5545 compliant calendar entries.
 */
public class CalendarUtil {

    private static final DateTimeFormatter ICS_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");

    /**
     * Generate an iCalendar (.ics) content string for an appointment.
     *
     * @param summary       Event title (e.g., "Appointment with Dr. Smith")
     * @param description   Event description
     * @param location      Event location (clinic address or "Virtual")
     * @param startTime     Event start time (in Amman timezone)
     * @param endTime       Event end time (in Amman timezone)
     * @param meetLink      Optional Google Meet link for virtual consultations
     * @return iCalendar formatted string
     */
    public static String generateIcsContent(
            String summary,
            String description,
            String location,
            ZonedDateTime startTime,
            ZonedDateTime endTime,
            String meetLink
    ) {
        String uid = UUID.randomUUID().toString();
        String now = ZonedDateTime.now().format(ICS_DATE_FORMAT);
        String startFormatted = startTime.format(ICS_DATE_FORMAT);
        String endFormatted = endTime.format(ICS_DATE_FORMAT);

        // Build description with meet link if provided
        String fullDescription = description;
        if (meetLink != null && !meetLink.isBlank()) {
            fullDescription += "\\n\\nJoin via Google Meet: " + meetLink;
        }

        // Escape special characters for iCalendar format
        summary = escapeIcsText(summary);
        fullDescription = escapeIcsText(fullDescription);
        location = escapeIcsText(location);

        StringBuilder ics = new StringBuilder();
        ics.append("BEGIN:VCALENDAR\r\n");
        ics.append("VERSION:2.0\r\n");
        ics.append("PRODID:-//Qadri's Clinic//Appointment System//EN\r\n");
        ics.append("CALSCALE:GREGORIAN\r\n");
        ics.append("METHOD:REQUEST\r\n");
        ics.append("BEGIN:VEVENT\r\n");
        ics.append("UID:").append(uid).append("\r\n");
        ics.append("DTSTAMP:").append(now).append("\r\n");
        ics.append("DTSTART:").append(startFormatted).append("\r\n");
        ics.append("DTEND:").append(endFormatted).append("\r\n");
        ics.append("SUMMARY:").append(summary).append("\r\n");
        ics.append("DESCRIPTION:").append(fullDescription).append("\r\n");
        ics.append("LOCATION:").append(location).append("\r\n");
        ics.append("STATUS:CONFIRMED\r\n");
        ics.append("SEQUENCE:0\r\n");

        // Add alarm/reminder 30 minutes before
        ics.append("BEGIN:VALARM\r\n");
        ics.append("TRIGGER:-PT30M\r\n");
        ics.append("ACTION:DISPLAY\r\n");
        ics.append("DESCRIPTION:Appointment Reminder\r\n");
        ics.append("END:VALARM\r\n");

        ics.append("END:VEVENT\r\n");
        ics.append("END:VCALENDAR\r\n");

        return ics.toString();
    }

    /**
     * Generate Google Calendar URL for adding an event.
     *
     * @param title      Event title
     * @param details    Event details/description
     * @param location   Event location
     * @param startTime  Event start time (in Amman timezone)
     * @param endTime    Event end time (in Amman timezone)
     * @return Google Calendar add event URL
     */
    public static String generateGoogleCalendarUrl(
            String title,
            String details,
            String location,
            ZonedDateTime startTime,
            ZonedDateTime endTime
    ) {
        // Format: yyyyMMddTHHmmss
        DateTimeFormatter googleFormat = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
        String startFormatted = startTime.format(googleFormat);
        String endFormatted = endTime.format(googleFormat);

        // URL encode parameters
        String encodedTitle = urlEncode(title);
        String encodedDetails = urlEncode(details);
        String encodedLocation = urlEncode(location);

        return "https://calendar.google.com/calendar/render?action=TEMPLATE" +
                "&text=" + encodedTitle +
                "&details=" + encodedDetails +
                "&location=" + encodedLocation +
                "&dates=" + startFormatted + "/" + endFormatted;
    }

    /**
     * Escape special characters for iCalendar text fields.
     */
    private static String escapeIcsText(String text) {
        if (text == null) {
            return "";
        }
        return text
                .replace("\\", "\\\\")
                .replace(",", "\\,")
                .replace(";", "\\;")
                .replace("\n", "\\n")
                .replace("\r", "");
    }

    /**
     * Simple URL encoding for calendar parameters.
     */
    private static String urlEncode(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace(" ", "+")
                .replace("\n", "%0A")
                .replace("&", "%26")
                .replace("=", "%3D")
                .replace(",", "%2C");
    }
}
