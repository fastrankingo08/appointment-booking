package com.fastranking.appointment_booking.repository;

import com.fastranking.appointment_booking.entity.Slot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SlotRepository extends JpaRepository<Slot , Integer> {
    @Query("SELECT s FROM Slot s WHERE s.isActive = true")
    List<Slot> findActiveSlots();
}

