package com.nirvanafire.ocadmin.repository;

import com.nirvanafire.ocadmin.entity.MeetingAttendee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MeetingAttendeeRepository extends JpaRepository<MeetingAttendee, Long> {
    
    List<MeetingAttendee> findByReservationId(Long reservationId);
    
    Optional<MeetingAttendee> findByReservationIdAndUserId(Long reservationId, Long userId);
    
    Long countByReservationIdAndIsCheckedInTrue(Long reservationId);
}
