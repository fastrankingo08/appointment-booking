package com.fastranking.appointment_booking.repository;

import com.fastranking.appointment_booking.entity.Agent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AgentRepository extends JpaRepository<Agent , Integer> {

    @Query("SELECT a.id FROM Agent a WHERE a.isActive = true")
    List<Integer> findActiveAgentIds();


}
