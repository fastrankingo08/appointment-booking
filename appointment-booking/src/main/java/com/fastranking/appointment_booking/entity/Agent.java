package com.fastranking.appointment_booking.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "agents" , indexes = {
                @Index(name = "idx_is_active" , columnList = "is_active")
}
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Agent {
    @Id
    private Integer id;
    private String name;

    @Column(unique = true)
    private String email;

    @Column(name = "is_active")
    private Boolean isActive;
}
