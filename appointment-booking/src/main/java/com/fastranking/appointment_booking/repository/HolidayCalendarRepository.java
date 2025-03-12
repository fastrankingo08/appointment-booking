package com.fastranking.appointment_booking.repository;

import com.fastranking.appointment_booking.entity.HolidayCalendar;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface HolidayCalendarRepository extends JpaRepository<HolidayCalendar, Integer> {
    Optional<HolidayCalendar> findByHolidayDate(LocalDate holidayDate);
}
