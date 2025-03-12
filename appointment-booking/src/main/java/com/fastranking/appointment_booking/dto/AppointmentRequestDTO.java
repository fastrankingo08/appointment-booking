package com.fastranking.appointment_booking.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentRequestDTO {
    // Expected format: "yyyy-MM-dd"
    @JsonProperty("appointment_date")
    private LocalDate appointmentDate;
    // The ID of the selected slot
    @JsonProperty("slot_id")
    private Integer slotId;
}
