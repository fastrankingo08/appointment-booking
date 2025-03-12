package com.fastranking.appointment_booking.controller;
import com.fastranking.appointment_booking.dto.AppointmentRequestDTO;
import com.fastranking.appointment_booking.dto.AppointmentResponseDTO;
import com.fastranking.appointment_booking.dto.AvailableSlotDTO;
import com.fastranking.appointment_booking.entity.Appointment;
import com.fastranking.appointment_booking.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final ModelMapper modelMapper;

    // When the user selects a date on the front-end, the API is hit with the date parameter
    @GetMapping("/available-slots")
    public ResponseEntity<Map<String, Object>> getAvailableSlots(
            @RequestParam("appointment_date")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate appointmentDate) {
        List<AvailableSlotDTO> slots = appointmentService.getAvailableSlots(appointmentDate);
        String message = slots.isEmpty() ? "No appointments can be booked on this day." : "";
        return ResponseEntity.ok(Map.of(
                "success", true,
                "appointment_date", appointmentDate,
                "availableSlots", slots,
                "message", message
        ));
    }

    @PostMapping("/book")
    public ResponseEntity<Map<String , Object>> bookAppointment(@RequestBody AppointmentRequestDTO appointmentRequestDTO){
        Appointment appointment = appointmentService.bookAppointment(appointmentRequestDTO);

        AppointmentResponseDTO responseDTO = modelMapper.map(appointment, AppointmentResponseDTO.class);
        Map<String , Object> response =  Map.of(
                "success", true,
                "message", "Appointment booked successfully.",
                "appointment", responseDTO
        );
        return ResponseEntity.ok(response);
    }
}
