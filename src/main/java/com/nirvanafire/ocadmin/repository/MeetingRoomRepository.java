package com.nirvanafire.ocadmin.repository;

import com.nirvanafire.ocadmin.entity.MeetingRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MeetingRoomRepository extends JpaRepository<MeetingRoom, Long> {
    List<MeetingRoom> findByEnabledTrue();
    
    @Query("SELECT mr FROM MeetingRoom mr WHERE mr.enabled = true AND mr.id NOT IN " +
           "(SELECT r.roomId FROM MeetingReservation r WHERE r.startTime < :endTime AND r.endTime > :startTime AND r.status = 'SCHEDULED')")
    List<MeetingRoom> findAvailableRooms(java.time.LocalDateTime startTime, java.time.LocalDateTime endTime);
}
