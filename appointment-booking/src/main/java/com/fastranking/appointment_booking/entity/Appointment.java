package com.fastranking.appointment_booking.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(
        name = "appointments",
        indexes = {
                @Index(name = "idx_slot_id", columnList = "slot_id"),
                @Index(name = "idx_lead_id", columnList = "lead_id"),
                @Index(name = "fk_appointments_agent", columnList = "agent_id"),
                @Index(name = "idx_is_temporary_assigned", columnList = "is_temporary_assigned"),
                @Index(name = "idx_is_reserved", columnList = "is_reserved"),
                @Index(name = "idx_reserved_reason", columnList = "reserved_reason"),
                @Index(name = "idx_status", columnList = "status"),
                @Index(name = "idx_appointment_date", columnList = "appointment_date")
        }
)
@NoArgsConstructor
@AllArgsConstructor
@ToString

public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // AUTO_INCREMENT in the DB
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "agent_id", referencedColumnName = "id", nullable = false)
    private Agent agent; // Many appointments are associated with one Agent

    @ManyToOne
    @JoinColumn(name = "slot_id", referencedColumnName = "id", nullable = false)
    private Slot slot; // Many appointments are associated with one Slot (assuming a Slot entity exists)

    @ManyToOne
    @JoinColumn(name = "lead_id", referencedColumnName = "id")
    private Lead lead; // Many appointments can be associated with one Lead (assuming a Lead entity exists)

    @Column(name = "appointment_date")
    private LocalDate appointmentDate; // Date and time of the appointment

    @Column(name = "is_temporary_assigned", columnDefinition = "TINYINT(1)", nullable = false)
    private Boolean isTemporaryAssigned = true; // Temporary assignment flag

    @Column(name = "is_reserved", columnDefinition = "TINYINT(1)")
    private Boolean isReserved = false; // Reserved flag

    @Column(name = "reserved_reason", length = 255)
    private String reservedReason; // Reason for reservation

    @Column(name = "status", length = 50)
    private String status; // Status of the appointment

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Lifecycle callback to set updated_at
    @PrePersist
    @PreUpdate
    public void setTimestamps() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        this.updatedAt = LocalDateTime.now();
    }

}
