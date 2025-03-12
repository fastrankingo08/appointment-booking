package com.fastranking.appointment_booking.repository;

import com.fastranking.appointment_booking.entity.AgentLeave;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface AgentLeaveRepository extends JpaRepository<AgentLeave , Integer> {

    List<AgentLeave> findByLeaveDate(LocalDate leaveDate);

}
