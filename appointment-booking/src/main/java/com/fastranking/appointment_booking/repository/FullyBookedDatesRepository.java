package com.fastranking.appointment_booking.repository;

import com.fastranking.appointment_booking.entity.FullyBookedDates;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface FullyBookedDatesRepository extends JpaRepository<FullyBookedDates , Integer> {
    Optional<FullyBookedDates> findByDate(LocalDate date);
}
