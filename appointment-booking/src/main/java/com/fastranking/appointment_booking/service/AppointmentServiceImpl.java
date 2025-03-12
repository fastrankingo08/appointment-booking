package com.fastranking.appointment_booking.service;

import com.fastranking.appointment_booking.dto.AppointmentRequestDTO;
import com.fastranking.appointment_booking.dto.AvailableSlotDTO;
import com.fastranking.appointment_booking.entity.*;
import com.fastranking.appointment_booking.exception.ResourceNotFoundException;
import com.fastranking.appointment_booking.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.internal.asm.tree.TryCatchBlockNode;
import org.springframework.stereotype.Service;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    private final HolidayCalendarRepository holidayCalendarRepository;
    private final FullyBookedDatesRepository fullyBookedDatesRepository;
    private final ConfigurationRepository configurationRepository;
    private final AgentRepository agentRepository;
    private final AgentLeaveRepository agentLeaveRepository;
    private final SlotRepository slotRepository;
    private final AppointmentRepository appointmentRepository;

    @Override
    public List<AvailableSlotDTO> getAvailableSlots(LocalDate appointmentDate) {

        // 1. Validate input
        if (appointmentDate == null) {
            throw new IllegalArgumentException("Appointment date is required.");
        }

        // 2. Fully Booked Check
        if (fullyBookedDatesRepository.findByDate(appointmentDate).isPresent()) {
            return Collections.emptyList();
        }

        // 3. Holiday Check
        Optional<HolidayCalendar> holidayOpt = holidayCalendarRepository.findByHolidayDate(appointmentDate);
        String holidayType = null;
        if (holidayOpt.isPresent()) {
            HolidayCalendar holiday = holidayOpt.get();
            if (holiday.getHolidayType().name().equalsIgnoreCase("FULL_DAY")) {
                return Collections.emptyList();
            } else {
                holidayType = holiday.getHolidayType().name();
            }
        }

        // 4. Load Global Configurations
        Map<String, String> configs = configurationRepository.getAllAsMap();
        String firstHalfStart = configs.getOrDefault("first_half_start", "09:00:00");
        String firstHalfEnd = configs.getOrDefault("first_half_end", "13:30:00");
        String secondHalfStart = configs.getOrDefault("second_half_start", "13:30:00");
        String secondHalfEnd = configs.getOrDefault("second_half_end", "18:00:00");

        // Create final variables for lambda usage.
        final String finalHolidayType = holidayType;
        final String finalFirstHalfStart = firstHalfStart;
        final String finalFirstHalfEnd = firstHalfEnd;
        final String finalSecondHalfStart = secondHalfStart;
        final String finalSecondHalfEnd = secondHalfEnd;

        // 5. Fetch Active Agents
        List<Integer> activeAgentIds = agentRepository.findActiveAgentIds();

        // 6. Fetch Agent Leaves for the Date
        List<AgentLeave> agentLeaves = agentLeaveRepository.findByLeaveDate(appointmentDate);

        // 7. Fetch Active Slots
        List<Slot> slots = slotRepository.findActiveSlots();

        // 8. Pre-filter slots based on holiday type:
        //    Remove slots that fall within the blocked half-day period.
        List<Slot> filteredSlots = slots.stream()
                .filter(slot -> {
                    String slotStartTimeStr = slot.getStartTime().toString();
                    return !(
                            ("FIRST_HALF".equalsIgnoreCase(finalHolidayType) && isTimeInRange(slotStartTimeStr, finalFirstHalfStart, finalFirstHalfEnd)) ||
                                    ("SECOND_HALF".equalsIgnoreCase(finalHolidayType) && isTimeInRange(slotStartTimeStr, finalSecondHalfStart, finalSecondHalfEnd))
                    );
                })
                .collect(Collectors.toList());

        // 9. Retrieve Appointment Counts by Slot
        List<Object[]> appointmentCounts = appointmentRepository.countAppointmentsBySlot(appointmentDate);
        Map<Long, Long> appointmentsBySlot = new HashMap<>();
        for (Object[] row : appointmentCounts) {
            Long slotId = (Long) row[0];
            Long bookedCount = (Long) row[1];
            appointmentsBySlot.put(slotId, bookedCount);
        }

        // 10. Process each filtered slot to compute available capacity
        List<AvailableSlotDTO> availableSlots = new ArrayList<>();
        for (Slot slot : filteredSlots) {
            String slotStartTimeStr = slot.getStartTime().toString();
            int totalCapacity = getTotalCapacityForSlot(slotStartTimeStr, finalHolidayType,
                    finalFirstHalfStart, finalFirstHalfEnd, finalSecondHalfStart, finalSecondHalfEnd,
                    activeAgentIds, agentLeaves);
            long bookedCount = appointmentsBySlot.getOrDefault(slot.getId(), 0L);
            int availableCapacity = totalCapacity - (int) bookedCount;
            if (availableCapacity > 0) {
                availableSlots.add(new AvailableSlotDTO(slot.getId(), slot.getStartTime().toString(),
                        slot.getEndTime().toString(), availableCapacity));
            }
        }
        return availableSlots;
    }



    // Helper: Check if a time (HH:mm:ss) is in the range [start, end)
    private boolean isTimeInRange(String time, String start, String end) {
        return time.compareTo(start) >= 0 && time.compareTo(end) < 0;
    }
    private boolean isTimeInRange2(LocalTime time, LocalTime start, LocalTime end) {
        return !time.isBefore(start) && time.isBefore(end);
    }


    // Helper: Calculate total capacity for a slot based on agent leaves and holiday type
    private int getTotalCapacityForSlot(String slotStartTime, String holidayType,
                                        String firstHalfStart, String firstHalfEnd,
                                        String secondHalfStart, String secondHalfEnd,
                                        List<Integer> allAgentIds, List<AgentLeave> agentLeaves) {
        // Block the corresponding half if there's a half-day holiday
        if ("FIRST_HALF".equalsIgnoreCase(holidayType) && isTimeInRange(slotStartTime, firstHalfStart, firstHalfEnd)) {
            return 0;
        }
        if ("SECOND_HALF".equalsIgnoreCase(holidayType) && isTimeInRange(slotStartTime, secondHalfStart, secondHalfEnd)) {
            return 0;
        }
        // Calculate capacity based on leaves for the slot’s time range
        List<AgentLeave> leavesForSlot;
        if (isTimeInRange(slotStartTime, firstHalfStart, firstHalfEnd)) {
            leavesForSlot = agentLeaves.stream().filter(leave ->
                    leave.getLeaveOf().name().equalsIgnoreCase("FULL_DAY") ||
                            leave.getLeaveOf().name().equalsIgnoreCase("FIRST_HALF")
            ).collect(Collectors.toList());
        } else if (isTimeInRange(slotStartTime, secondHalfStart, secondHalfEnd)) {
            leavesForSlot = agentLeaves.stream().filter(leave ->
                    leave.getLeaveOf().name().equalsIgnoreCase("FULL_DAY") ||
                            leave.getLeaveOf().name().equalsIgnoreCase("SECOND_HALF")
            ).collect(Collectors.toList());
        } else {
            leavesForSlot = Collections.emptyList();
        }
        // Count distinct agents on leave
        Set<Integer> agentsOnLeave = leavesForSlot.stream()
                .map(leave -> leave.getAgent().getId())
                .collect(Collectors.toSet());
        // Return the number of active agents not on leave
        return (int) allAgentIds.stream().filter(id -> !agentsOnLeave.contains(id)).count();
    }


    @Override
    @Transactional
    public Appointment bookAppointment(AppointmentRequestDTO appointmentRequestDTO ) {
        if (appointmentRequestDTO.getAppointmentDate() == null || appointmentRequestDTO.getSlotId() == null){
            throw  new IllegalArgumentException("Appointment Date and Slot Id both are required");
        }

        LocalDate appointmentDate = appointmentRequestDTO.getAppointmentDate();
        Integer slotId = appointmentRequestDTO.getSlotId();


        // 2. Retrieve configuration for maximum calls per agent.
        String maxCallStr = configurationRepository.getValue("max_call_per_agent");
        int maxCall = 10;
        if (maxCallStr != null) {
            try {
                maxCall = Integer.parseInt(maxCallStr);
            } catch (NumberFormatException e) {
                maxCall = 10;
            }
        }

        // 3. Retrieve the selected slot details.

        Slot slot = slotRepository.findById(slotId).orElseThrow(()-> new ResourceNotFoundException("Selected Slot Not Found"));

        LocalTime slotStartTime = slot.getStartTime();

        // 4. Load global configuration settings (half-day boundaries).
        Map<String, String> configs = configurationRepository.getAllAsMap();
        String firstHalfStart = configs.getOrDefault("first_half_start", "09:00:00");
        String firstHalfEnd = configs.getOrDefault("first_half_end", "12:30:00");
        String secondHalfStart = configs.getOrDefault("second_half_start", "12:30:00");
        String secondHalfEnd = configs.getOrDefault("second_half_end", "18:00:00");

        // Convert configuration times to LocalTime.
        LocalTime firstHalfStartTime = LocalTime.parse(firstHalfStart);
        LocalTime firstHalfEndTime = LocalTime.parse(firstHalfEnd);
        LocalTime secondHalfStartTime = LocalTime.parse(secondHalfStart);
        LocalTime secondHalfEndTime = LocalTime.parse(secondHalfEnd);

        // 5. Fetch all active agents.
        List<Integer> allAgentIds = agentRepository.findActiveAgentIds();

        // 6. Retrieve all agent leaves for the appointment date.
        List<AgentLeave> agentLeaves = agentLeaveRepository.findByLeaveDate(appointmentDate);
        // 7. Filter available agents based on the slot's time period.

        List<AgentLeave> leavesForSlot;
        if (isTimeInRange2(slotStartTime, firstHalfStartTime, firstHalfEndTime)) {
            leavesForSlot = agentLeaves.stream()
                    .filter(leave ->
                    leave.getLeaveOf() == AgentLeave.LeaveType.FULL_DAY ||
                            leave.getLeaveOf() == AgentLeave.LeaveType.FIRST_HALF
            ).collect(Collectors.toList());
        } else if (isTimeInRange2(slotStartTime, secondHalfStartTime, secondHalfEndTime)) {
            leavesForSlot = agentLeaves.stream()
                    .filter(leave ->
                            leave.getLeaveOf() == AgentLeave.LeaveType.FULL_DAY ||
                                    leave.getLeaveOf() == AgentLeave.LeaveType.SECOND_HALF
                    ).collect(Collectors.toList());
        } else {
            leavesForSlot = new ArrayList<>();
        }

        List<Integer> agentIdsOnLeave = leavesForSlot.stream().map(
                leave -> leave.getAgent().getId()).distinct().collect(Collectors.toList());

        Set<Integer> availableAgentIds = new HashSet<>(allAgentIds);
        availableAgentIds.removeAll(agentIdsOnLeave);

        // 8. Exclude agents already assigned to the same slot on the appointment date.
        List<Integer> assignedAgentIdsForSlot = appointmentRepository
                .findAgentIdsByAppointmentDateAndSlotId(appointmentDate, slotId);

        availableAgentIds.removeAll(assignedAgentIdsForSlot);

        Integer selectedAgentId;
        if (availableAgentIds.isEmpty()) {
            throw new RuntimeException("No available agents for the selected slot on this date.");
        } else if (availableAgentIds.size() ==1) {
            selectedAgentId = availableAgentIds.iterator().next();
        }else {
            Map<Integer , Long> appointmentsByAgent = appointmentRepository.getAppointmentCountsForAvailableAgents(appointmentDate , availableAgentIds);

            final int maxCallFinal = maxCall;

            long minCount = availableAgentIds.stream()
                    .mapToLong(id -> appointmentsByAgent.getOrDefault(id , 0L))
                    .min().orElse(0L);

            List<Integer> candidateAgents = availableAgentIds.stream()
                    .filter(id -> appointmentsByAgent.getOrDefault(id, 0L) == minCount && appointmentsByAgent.getOrDefault(id, 0L) < maxCallFinal)
                    .collect(Collectors.toList());

            if (candidateAgents.isEmpty()) {
                throw new RuntimeException("All available agents have reached their maximum appointment limit for the selected date.");
            }

            // If only one candidate, use it; otherwise, select one at random.
            if (candidateAgents.size() == 1) {
                selectedAgentId = candidateAgents.get(0);
            } else {
                Random random = new Random();
                selectedAgentId = candidateAgents.get(random.nextInt(candidateAgents.size()));
            }
        }


        // 10. Create the appointment record.
        Appointment appointment = new Appointment();
        Agent agent = agentRepository.findById(selectedAgentId)
                .orElseThrow(() -> new ResourceNotFoundException("Agent not found"));
        appointment.setAgent(agent);
        appointment.setSlot(slot);
        // In this example, lead is not provided by the UI.
        appointment.setAppointmentDate(appointmentDate);
        appointment.setStatus("pending");
        appointment.setIsTemporaryAssigned(true);
        appointment.setIsReserved(false);
        appointment = appointmentRepository.save(appointment);


        // 11. Update Fully Booked Dates.
        Map<Long, Long> appointmentsBySlot = appointmentRepository.getAppointmentCountsBySlot(appointmentDate);
        List<Slot> activeSlots = slotRepository.findActiveSlots();
        HolidayCalendar holiday = holidayCalendarRepository.findByHolidayDate(appointmentDate)
                .orElse(null);
// Convert the holiday enum to its string representation.
        String holidayTypeStr = (holiday != null && holiday.getHolidayType() != null)
                ? holiday.getHolidayType().name()
                : null;

        Function<LocalTime, Integer> getTotalCapacityForSlot = (slotTime) -> {
            // If the holiday is in the first half and the slot's time falls in that range, capacity is 0.
            if ("first_half".equalsIgnoreCase(holidayTypeStr) &&
                    isTimeInRange2(slotTime, firstHalfStartTime, firstHalfEndTime)) {
                return 0;
            }
            // If the holiday is in the second half and the slot's time falls in that range, capacity is 0.
            if ("second_half".equalsIgnoreCase(holidayTypeStr) &&
                    isTimeInRange2(slotTime, secondHalfStartTime, secondHalfEndTime)) {
                return 0;
            }
            // Otherwise, determine capacity based on agent leaves.
            Set<Integer> capacityAgentIds = new HashSet<>(allAgentIds);
            List<AgentLeave> leaves;
            if (isTimeInRange2(slotTime, firstHalfStartTime, firstHalfEndTime)) {
                leaves = agentLeaves.stream()
                        .filter(leave -> leave.getLeaveOf() == AgentLeave.LeaveType.FULL_DAY ||
                                leave.getLeaveOf() == AgentLeave.LeaveType.FIRST_HALF)
                        .collect(Collectors.toList());
            } else if (isTimeInRange2(slotTime, secondHalfStartTime, secondHalfEndTime)) {
                leaves = agentLeaves.stream()
                        .filter(leave -> leave.getLeaveOf() == AgentLeave.LeaveType.FULL_DAY ||
                                leave.getLeaveOf() == AgentLeave.LeaveType.SECOND_HALF)
                        .collect(Collectors.toList());
            } else {
                leaves = new ArrayList<>();
            }
            Set<Integer> leaveAgentIds = leaves.stream()
                    .map(leave -> leave.getAgent().getId())
                    .collect(Collectors.toSet());
            capacityAgentIds.removeAll(leaveAgentIds);
            return capacityAgentIds.size();
        };


        boolean isDateFullyBooked = true;
        for (Slot activeSlot : activeSlots) {
            int totalCapacity = getTotalCapacityForSlot.apply(activeSlot.getStartTime());
            long bookedCount = appointmentsBySlot.getOrDefault(activeSlot.getId(), 0L);
            if (totalCapacity - bookedCount > 0) {
                isDateFullyBooked = false;
                break;
            }
        }
        if (isDateFullyBooked) {
            FullyBookedDates fullyBookedDates = fullyBookedDatesRepository.findByDate(appointmentDate)
                    .orElse(new FullyBookedDates());
            fullyBookedDates.setDate(appointmentDate);
            fullyBookedDates.setUpdatedAt(LocalDateTime.now());
            fullyBookedDatesRepository.save(fullyBookedDates);
        }

        return appointment;

    }
}
