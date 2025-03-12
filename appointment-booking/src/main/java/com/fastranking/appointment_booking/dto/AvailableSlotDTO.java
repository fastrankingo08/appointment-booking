package com.fastranking.appointment_booking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public class AvailableSlotDTO {
        private Long id;
        private String startTime;
        private String endTime;
        private int availableCapacity;
    }
