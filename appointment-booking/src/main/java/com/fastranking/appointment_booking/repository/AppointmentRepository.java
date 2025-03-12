package com.fastranking.appointment_booking.repository;

import com.fastranking.appointment_booking.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public interface AppointmentRepository extends JpaRepository<Appointment, Integer> {

    @Query("SELECT a.slot.id, COUNT(a.id) FROM Appointment a " +
            "WHERE FUNCTION('DATE_FORMAT', a.appointmentDate, '%Y-%m-%d') = :appointmentDate " +
            "GROUP BY a.slot.id")
    List<Object[]> countAppointmentsBySlot(@Param("appointmentDate") LocalDate appointmentDate);

    @Query("SELECT a.agent.id FROM Appointment a WHERE a.appointmentDate = :appointmentDate AND a.slot.id = :slotId")
    List<Integer> findAgentIdsByAppointmentDateAndSlotId(@Param("appointmentDate") LocalDate appointmentDate,
                                                         @Param("slotId") Integer slotId);

    @Query("SELECT a.agent.id as agentId, COUNT(a) as appointmentCount " +
            "FROM Appointment a WHERE a.appointmentDate = :appointmentDate " +
            "GROUP BY a.agent.id")
    List<Object[]> getAppointmentCountsByAgentRaw(@Param("appointmentDate") LocalDate appointmentDate);

    default Map<Integer, Long> getAppointmentCountsByAgent(LocalDate appointmentDate) {
        return getAppointmentCountsByAgentRaw(appointmentDate).stream()
                .collect(Collectors.toMap(
                        row -> (Integer) row[0],
                        row -> (Long) row[1]
                ));
    }

    // New: Raw query for available agents using IN clause.
    @Query("SELECT a.agent.id as agentId, COUNT(a) as appointmentCount " +
            "FROM Appointment a WHERE a.appointmentDate = :appointmentDate " +
            "AND a.agent.id IN :availableAgentIds " +
            "GROUP BY a.agent.id")
    List<Object[]> getAppointmentCountsForAvailableAgentsRaw(@Param("appointmentDate") LocalDate appointmentDate,
                                                             @Param("availableAgentIds") Set<Integer> availableAgentIds);

    default Map<Integer, Long> getAppointmentCountsForAvailableAgents(LocalDate appointmentDate, Set<Integer> availableAgentIds) {
        return getAppointmentCountsForAvailableAgentsRaw(appointmentDate, availableAgentIds).stream()
                .collect(Collectors.toMap(
                        row -> (Integer) row[0],
                        row -> (Long) row[1]
                ));
    }

    @Query("SELECT a.slot.id as slotId, COUNT(a) as bookedCount " +
            "FROM Appointment a WHERE a.appointmentDate = :appointmentDate " +
            "GROUP BY a.slot.id")
    List<Object[]> getAppointmentCountsBySlotRaw(@Param("appointmentDate") LocalDate appointmentDate);

    default Map<Long, Long> getAppointmentCountsBySlot(LocalDate appointmentDate) {
        return getAppointmentCountsBySlotRaw(appointmentDate).stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));
    }
}
