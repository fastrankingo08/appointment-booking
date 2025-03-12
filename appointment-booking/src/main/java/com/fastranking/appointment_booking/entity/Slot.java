package com.fastranking.appointment_booking.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Table(name = "slots", indexes = {
        @Index(name = "idx_start_time", columnList = "start_time"),
        @Index(name = "idx_end_time", columnList = "end_time"),
        @Index(name = "idx_is_active", columnList = "is_active")
})

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Slot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;
//    private java.sql.Time startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;
//    private java.sql.Time endTime;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Pre-update lifecycle callback to set updated_at on entity update
    @PreUpdate
    public void setUpdatedAt() {
        this.updatedAt = LocalDateTime.now(); // Set the current timestamp
    }
}
