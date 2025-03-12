package com.fastranking.appointment_booking.repository;

import com.fastranking.appointment_booking.entity.Lead;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeadRepository extends JpaRepository<Lead , Integer> {
}
