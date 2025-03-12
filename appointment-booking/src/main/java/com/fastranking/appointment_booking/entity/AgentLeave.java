package com.fastranking.appointment_booking.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "agent_leaves" , indexes = {
        @Index(name = "idx_agent_id", columnList = "agent_id"),
        @Index(name = "idx_leave_date", columnList = "leave_date"),
        @Index(name = "idx_leave_of", columnList = "leave_of")
}
)

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString

public class AgentLeave {

    // Enum for leave types
    public enum LeaveType {
        FULL_DAY,
        FIRST_HALF,
        SECOND_HALF
    }

    @Id
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "agent_id" , referencedColumnName = "id" , nullable = false)
    private Agent agent;

    @Column(name = "leave_date")
    private LocalDate leaveDate;


    @Column(name = "leave_of", columnDefinition = "enum('FULL_DAY', 'FIRST_HALF', 'SECOND_HALF')")
    @Enumerated(EnumType.STRING) // Enum mapping
    private LeaveType leaveOf;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Pre-update lifecycle callback to set updated_at on entity update
    @PreUpdate
    public void setUpdatedAt() {
        this.updatedAt = LocalDateTime.now(); // Set the current timestamp
    }
}
