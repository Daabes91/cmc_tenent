package com.clinic.modules.admin.util;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Utility class for handling date ranges in reports
 */
public class DateRange {
    private final LocalDate startDate;
    private final LocalDate endDate;
    
    private static final int MAX_RANGE_DAYS = 730; // 2 years
    
    private DateRange(LocalDate startDate, LocalDate endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }
    
    /**
     * Creates a DateRange from optional start and end dates
     * If dates are null, defaults to current month
     * 
     * @param start Start date (can be null)
     * @param end End date (can be null)
     * @return DateRange instance
     * @throws IllegalArgumentException if date range is invalid
     */
    public static DateRange of(LocalDate start, LocalDate end) {
        if (start == null && end == null) {
            // Default to current month
            LocalDate now = LocalDate.now();
            return new DateRange(
                now.withDayOfMonth(1),
                now.withDayOfMonth(now.lengthOfMonth())
            );
        }
        
        // If only one date is provided, set reasonable defaults
        if (start == null) {
            start = end.minusYears(1); // Default to 1 year before end date
        }
        if (end == null) {
            end = LocalDate.now(); // Default to today
        }
        
        // Validate the date range
        validateDateRange(start, end);
        
        return new DateRange(start, end);
    }
    
    /**
     * Creates a DateRange for the current month
     * 
     * @return DateRange for current month
     */
    public static DateRange currentMonth() {
        LocalDate now = LocalDate.now();
        return new DateRange(
            now.withDayOfMonth(1),
            now.withDayOfMonth(now.lengthOfMonth())
        );
    }
    
    /**
     * Creates a DateRange for the last N days
     * 
     * @param days Number of days to go back
     * @return DateRange for the last N days
     */
    public static DateRange lastDays(int days) {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(days - 1);
        return new DateRange(start, end);
    }
    
    /**
     * Validates a date range according to business rules
     * 
     * @param start Start date
     * @param end End date
     * @throws IllegalArgumentException if validation fails
     */
    private static void validateDateRange(LocalDate start, LocalDate end) {
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("End date must be after start date");
        }
        
        // Allow future dates for reports (e.g., to see scheduled appointments)
        // Removed the future date restriction
        
        long daysBetween = ChronoUnit.DAYS.between(start, end);
        if (daysBetween > MAX_RANGE_DAYS) {
            throw new IllegalArgumentException("Date range cannot exceed " + MAX_RANGE_DAYS + " days");
        }
    }
    
    /**
     * Gets the start date of the range
     * 
     * @return Start date
     */
    public LocalDate getStartDate() {
        return startDate;
    }
    
    /**
     * Gets the end date of the range
     * 
     * @return End date
     */
    public LocalDate getEndDate() {
        return endDate;
    }
    
    /**
     * Gets the number of days in the range (inclusive)
     * 
     * @return Number of days
     */
    public long getDays() {
        return ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }
    
    /**
     * Checks if the range contains the given date
     * 
     * @param date Date to check
     * @return true if date is within range
     */
    public boolean contains(LocalDate date) {
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }
    
    /**
     * Checks if this range overlaps with another range
     * 
     * @param other Other date range
     * @return true if ranges overlap
     */
    public boolean overlaps(DateRange other) {
        return !endDate.isBefore(other.startDate) && !startDate.isAfter(other.endDate);
    }
    
    @Override
    public String toString() {
        return String.format("DateRange{startDate=%s, endDate=%s, days=%d}", 
                           startDate, endDate, getDays());
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        DateRange dateRange = (DateRange) obj;
        return startDate.equals(dateRange.startDate) && endDate.equals(dateRange.endDate);
    }
    
    @Override
    public int hashCode() {
        return startDate.hashCode() * 31 + endDate.hashCode();
    }
}