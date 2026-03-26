package com.nirvanafire.ocadmin.controller;

import com.nirvanafire.ocadmin.dto.MeetingReservationDTO;
import com.nirvanafire.ocadmin.dto.MeetingRoomDTO;
import com.nirvanafire.ocadmin.entity.SysUser;
import com.nirvanafire.ocadmin.repository.UserRepository;
import com.nirvanafire.ocadmin.service.MeetingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 会议管理控制器
 */
@RestController
@RequestMapping("/api/meeting")
@RequiredArgsConstructor
public class MeetingController {

    private final MeetingService meetingService;
    private final UserRepository userRepository;

    // ==================== 会议室 ====================

    @GetMapping("/rooms")
    public ResponseEntity<List<MeetingRoomDTO>> getRooms() {
        return ResponseEntity.ok(meetingService.getRooms());
    }

    @PostMapping("/rooms")
    public ResponseEntity<MeetingRoomDTO> createRoom(
            Authentication authentication,
            @RequestBody MeetingRoomDTO dto) {
        Long userId = getCurrentUserId(authentication);
        return ResponseEntity.ok(meetingService.createRoom(userId, dto));
    }

    @PutMapping("/rooms/{id}")
    public ResponseEntity<MeetingRoomDTO> updateRoom(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody MeetingRoomDTO dto) {
        Long userId = getCurrentUserId(authentication);
        return ResponseEntity.ok(meetingService.updateRoom(userId, id, dto));
    }

    @DeleteMapping("/rooms/{id}")
    public ResponseEntity<Void> deleteRoom(
            Authentication authentication,
            @PathVariable Long id) {
        Long userId = getCurrentUserId(authentication);
        meetingService.deleteRoom(userId, id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/rooms/available")
    public ResponseEntity<List<MeetingRoomDTO>> getAvailableRooms(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        return ResponseEntity.ok(meetingService.getAvailableRooms(startTime, endTime));
    }

    // ==================== 会议预约 ====================

    @PostMapping("/reservations")
    public ResponseEntity<MeetingReservationDTO> createReservation(
            Authentication authentication,
            @RequestBody MeetingReservationDTO dto) {
        Long userId = getCurrentUserId(authentication);
        String username = authentication.getName();
        return ResponseEntity.ok(meetingService.createReservation(userId, username, dto));
    }

    @GetMapping("/reservations/my")
    public ResponseEntity<Page<MeetingReservationDTO>> getMyReservations(
            Authentication authentication,
            Pageable pageable) {
        Long userId = getCurrentUserId(authentication);
        return ResponseEntity.ok(meetingService.getMyReservations(userId, pageable));
    }

    @GetMapping("/reservations/{id}")
    public ResponseEntity<MeetingReservationDTO> getReservation(@PathVariable Long id) {
        return ResponseEntity.ok(meetingService.getReservation(id));
    }

    @DeleteMapping("/reservations/{id}")
    public ResponseEntity<Void> cancelReservation(
            Authentication authentication,
            @PathVariable Long id) {
        Long userId = getCurrentUserId(authentication);
        meetingService.cancelReservation(userId, id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reservations/{id}/checkin")
    public ResponseEntity<Void> checkin(
            Authentication authentication,
            @PathVariable Long id) {
        Long userId = getCurrentUserId(authentication);
        meetingService.checkin(userId, id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reservations/{id}/minutes")
    public ResponseEntity<MeetingReservationDTO> addMinutes(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        Long userId = getCurrentUserId(authentication);
        String minutes = body.get("minutes");
        String attachmentUrls = body.get("attachmentUrls");
        return ResponseEntity.ok(meetingService.addMinutes(userId, id, minutes, attachmentUrls));
    }

    // ==================== 辅助方法 ====================

    private Long getCurrentUserId(Authentication authentication) {
        String username = authentication.getName();
        SysUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        return user.getId();
    }
}
