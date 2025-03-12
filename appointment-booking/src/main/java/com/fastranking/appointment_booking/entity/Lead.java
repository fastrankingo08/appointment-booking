package com.fastranking.appointment_booking.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "leads", indexes = {
        @Index(name = "idx_agent_id", columnList = "agent_id")
})

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "agent_id", nullable = false)
    private Long agentId;

    @Column(name = "customer_name", length = 255, nullable = false)
    private String customerName;

    @Column(name = "contact", length = 50, nullable = false)
    private String contact;

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
