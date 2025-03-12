
package com.fastranking.appointment_booking.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Table(
        name = "fully_booked_dates",  // Replace with actual table name
        indexes = {
                @Index(name = "idx_date", columnList = "date")
        }
)
public class FullyBookedDates {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // AUTO_INCREMENT in the DB
    private Integer id;

    @Column(name = "date", nullable = false)
    private LocalDate date; // Date field

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt; // Timestamp for creation

    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // Timestamp for last update

    // Lifecycle callback to set created_at and updated_at
    @PrePersist
    @PreUpdate
    public void setTimestamps() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        this.updatedAt = LocalDateTime.now();
    }
}
