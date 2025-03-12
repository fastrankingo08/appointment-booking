package com.fastranking.appointment_booking.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Table(
        name = "configuration",  // Replace with actual table name
        indexes = {
                @Index(name = "idx_conf_key", columnList = "conf_key")
        }
)
public class Configuration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // AUTO_INCREMENT in the DB
    private Integer id;

    @Column(name = "conf_key", unique = true, nullable = false, length = 255)
    private String key; // Unique key field

    @Column(name = "value", nullable = false, columnDefinition = "TEXT")
    private String value; // Text field for storing the value

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
