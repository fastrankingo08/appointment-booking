package com.fastranking.appointment_booking.service;

import com.fastranking.appointment_booking.dto.AppointmentRequestDTO;
import com.fastranking.appointment_booking.dto.AvailableSlotDTO;
import com.fastranking.appointment_booking.entity.Appointment;

import java.time.LocalDate;
import java.util.List;

public interface AppointmentService {
    List<AvailableSlotDTO> getAvailableSlots(LocalDate appointmentDate);
    Appointment bookAppointment(AppointmentRequestDTO appointmentRequestDTO);
}
