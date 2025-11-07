package com.clinic.modules.core.doctor;

import jakarta.persistence.*;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "doctor_availability")
public class DoctorAvailabilityEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "doctor_id", nullable = false)
    private DoctorEntity doctor;

    @Column(name = "recurring_weekly", nullable = false)
    private boolean recurringWeekly;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week")
    private DayOfWeek dayOfWeek;

    @Column(name = "specific_date")
    private LocalDate specificDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected DoctorAvailabilityEntity() {
    }

    public DoctorAvailabilityEntity(DoctorEntity doctor,
                                    boolean recurringWeekly,
                                    DayOfWeek dayOfWeek,
                                    LocalDate specificDate,
                                    LocalTime startTime,
                                    LocalTime endTime) {
        this.doctor = doctor;
        this.recurringWeekly = recurringWeekly;
        this.dayOfWeek = dayOfWeek;
        this.specificDate = specificDate;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    @PrePersist
    public void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public DoctorEntity getDoctor() {
        return doctor;
    }

    public boolean isRecurringWeekly() {
        return recurringWeekly;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public LocalDate getSpecificDate() {
        return specificDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void update(boolean recurringWeekly,
                       DayOfWeek dayOfWeek,
                       LocalDate specificDate,
                       LocalTime startTime,
                       LocalTime endTime) {
        this.recurringWeekly = recurringWeekly;
        this.dayOfWeek = dayOfWeek;
        this.specificDate = specificDate;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
