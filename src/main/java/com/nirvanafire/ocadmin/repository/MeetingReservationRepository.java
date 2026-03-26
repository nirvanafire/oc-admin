package com.nirvanafire.ocadmin.repository;

import com.nirvanafire.ocadmin.entity.MeetingReservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MeetingReservationRepository extends JpaRepository<MeetingReservation, Long> {
    
    Page<MeetingReservation> findByOrganizerId(Long organizerId, Pageable pageable);
    
    @Query("SELECT mr FROM MeetingReservation mr WHERE mr.roomId = :roomId AND mr.status = 'SCHEDULED' AND mr.startTime < :endTime AND mr.endTime > :startTime")
    List<MeetingReservation> findConflictingReservations(@Param("roomId") Long roomId, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    
    Optional<MeetingReservation> findByCheckinCode(String checkinCode);
    
    @Query("SELECT mr FROM MeetingReservation mr WHERE mr.startTime BETWEEN :startTime AND :endTime")
    List<MeetingReservation> findByTimeRange(LocalDateTime startTime, LocalDateTime endTime);
}
