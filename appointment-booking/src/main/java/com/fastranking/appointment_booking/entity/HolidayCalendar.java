package com.fastranking.appointment_booking.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;


@Entity
@Table(name = "holiday_calendars", indexes = {
        @Index(name = "idx_holiday_date", columnList = "holiday_date"),
        @Index(name = "idx_holiday_type", columnList = "holiday_type")
})

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class HolidayCalendar {

    public enum HolidayType {
        FULL_DAY,
        FIRST_HALF,
        SECOND_HALF
    }

    @Id
    private Integer id;


    @Column(name = "holiday_date", nullable = false)
    private LocalDate holidayDate;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "holiday_type", nullable = false)
    private HolidayType holidayType;

    @PreUpdate
    public void setUpdatedAt() {
        this.updatedAt = LocalDateTime.now(); // Set the current timestamp
    }
}
