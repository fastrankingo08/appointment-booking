package com.fastranking.appointment_booking.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class AppointmentResponseDTO {
    private Integer id;
    private AgentResponseDTO agent;
    private SlotResponseDTO slot;
    private LocalDate appointmentDate;
    private String status;
}
