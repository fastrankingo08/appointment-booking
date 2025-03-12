package com.fastranking.appointment_booking.dto;

import lombok.Data;

import java.time.LocalTime;

@Data
public class SlotResponseDTO {
    private Integer id;
    private LocalTime startTime;
    private LocalTime endTime;
}
