package com.nirvanafire.ocadmin.service.impl;

import com.nirvanafire.ocadmin.common.exception.BusinessException;
import com.nirvanafire.ocadmin.dto.MeetingReservationDTO;
import com.nirvanafire.ocadmin.dto.MeetingRoomDTO;
import com.nirvanafire.ocadmin.entity.MeetingAttendee;
import com.nirvanafire.ocadmin.entity.MeetingReservation;
import com.nirvanafire.ocadmin.entity.MeetingRoom;
import com.nirvanafire.ocadmin.repository.*;
import com.nirvanafire.ocadmin.service.MeetingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 会议服务实现
 */
@Service
@RequiredArgsConstructor
public class MeetingServiceImpl implements MeetingService {

    private final MeetingRoomRepository roomRepository;
    private final MeetingReservationRepository reservationRepository;
    private final MeetingAttendeeRepository attendeeRepository;
    private final UserRepository userRepository;

    // ==================== 会议室 ====================

    @Override
    public List<MeetingRoomDTO> getRooms() {
        return roomRepository.findByEnabledTrue().stream()
                .map(this::toRoomDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public MeetingRoomDTO createRoom(Long userId, MeetingRoomDTO dto) {
        MeetingRoom room = MeetingRoom.builder()
                .name(dto.getName())
                .location(dto.getLocation())
                .capacity(dto.getCapacity())
                .equipment(dto.getEquipment())
                .enabled(true)
                .creatorId(userId)
                .build();
        room = roomRepository.save(room);
        return toRoomDTO(room);
    }

    @Override
    @Transactional
    public MeetingRoomDTO updateRoom(Long userId, Long roomId, MeetingRoomDTO dto) {
        MeetingRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException("会议室不存在"));
        
        if (dto.getName() != null) room.setName(dto.getName());
        if (dto.getLocation() != null) room.setLocation(dto.getLocation());
        if (dto.getCapacity() != null) room.setCapacity(dto.getCapacity());
        if (dto.getEquipment() != null) room.setEquipment(dto.getEquipment());
        
        room = roomRepository.save(room);
        return toRoomDTO(room);
    }

    @Override
    @Transactional
    public void deleteRoom(Long userId, Long roomId) {
        MeetingRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException("会议室不存在"));
        
        // 检查是否有进行中的会议
        List<MeetingReservation> conflicts = reservationRepository.findConflictingReservations(
                roomId, LocalDateTime.now(), LocalDateTime.now().plusYears(1));
        if (!conflicts.isEmpty()) {
            throw new BusinessException("该会议室有未完成的会议预约，无法删除");
        }
        
        room.setEnabled(false);
        roomRepository.save(room);
    }

    @Override
    public List<MeetingRoomDTO> getAvailableRooms(LocalDateTime startTime, LocalDateTime endTime) {
        return roomRepository.findAvailableRooms(startTime, endTime).stream()
                .map(this::toRoomDTO)
                .collect(Collectors.toList());
    }

    // ==================== 会议预约 ====================

    @Override
    @Transactional
    public MeetingReservationDTO createReservation(Long userId, String username, MeetingReservationDTO dto) {
        MeetingRoom room = roomRepository.findById(dto.getRoomId())
                .orElseThrow(() -> new BusinessException("会议室不存在"));

        if (!room.getEnabled()) {
            throw new BusinessException("会议室已停用");
        }

        // 检查时间冲突
        List<MeetingReservation> conflicts = reservationRepository.findConflictingReservations(
                dto.getRoomId(), dto.getStartTime(), dto.getEndTime());
        if (!conflicts.isEmpty()) {
            throw new BusinessException("该时间段会议室已被预约");
        }

        MeetingReservation reservation = MeetingReservation.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .roomId(dto.getRoomId())
                .roomName(room.getName())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .organizerId(userId)
                .organizerName(username)
                .status("SCHEDULED")
                .checkinCode(UUID.randomUUID().toString().substring(0, 6).toUpperCase())
                .build();

        reservation = reservationRepository.save(reservation);

        // 添加参会人
        if (dto.getAttendeeIds() != null && !dto.getAttendeeIds().isEmpty()) {
            for (Long attendeeId : dto.getAttendeeIds()) {
                var user = userRepository.findById(attendeeId).orElse(null);
                MeetingAttendee attendee = MeetingAttendee.builder()
                        .reservationId(reservation.getId())
                        .userId(attendeeId)
                        .userName(user != null ? user.getNickname() : null)
                        .isCheckedIn(false)
                        .build();
                attendeeRepository.save(attendee);
            }
        }

        return toReservationDTO(reservation);
    }

    @Override
    public Page<MeetingReservationDTO> getMyReservations(Long userId, Pageable pageable) {
        Page<MeetingReservation> page = reservationRepository.findByOrganizerId(userId, pageable);
        List<MeetingReservationDTO> list = page.getContent().stream()
                .map(this::toReservationDTO)
                .collect(Collectors.toList());
        return new PageImpl<>(list, pageable, page.getTotalElements());
    }

    @Override
    public MeetingReservationDTO getReservation(Long reservationId) {
        MeetingReservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new BusinessException("会议不存在"));
        return toReservationDTO(reservation);
    }

    @Override
    @Transactional
    public void cancelReservation(Long userId, Long reservationId) {
        MeetingReservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new BusinessException("会议不存在"));

        if (!reservation.getOrganizerId().equals(userId)) {
            throw new BusinessException("无权操作");
        }

        reservation.setStatus("CANCELLED");
        reservationRepository.save(reservation);
    }

    @Override
    @Transactional
    public void checkin(Long userId, Long reservationId) {
        MeetingReservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new BusinessException("会议不存在"));

        MeetingAttendee attendee = attendeeRepository.findByReservationIdAndUserId(reservationId, userId)
                .orElseThrow(() -> new BusinessException("您不是该会议的参会人"));

        if (attendee.getIsCheckedIn()) {
            throw new BusinessException("已签到");
        }

        attendee.setIsCheckedIn(true);
        attendee.setCheckinTime(LocalDateTime.now());
        attendeeRepository.save(attendee);

        // 更新签到人数
        Long count = attendeeRepository.countByReservationIdAndIsCheckedInTrue(reservationId);
        reservation.setCheckinCount(count.intValue());
        reservationRepository.save(reservation);
    }

    @Override
    @Transactional
    public MeetingReservationDTO addMinutes(Long userId, Long reservationId, String minutes, String attachmentUrls) {
        MeetingReservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new BusinessException("会议不存在"));

        if (!reservation.getOrganizerId().equals(userId)) {
            throw new BusinessException("无权操作");
        }

        reservation.setMinutes(minutes);
        if (attachmentUrls != null) {
            reservation.setAttachmentUrls(attachmentUrls);
        }

        reservation = reservationRepository.save(reservation);
        return toReservationDTO(reservation);
    }

    @Override
    public Object getRoomStatistics(Long roomId, int year, int month) {
        // TODO: 实现统计逻辑
        return null;
    }

    // ==================== 转换方法 ====================

    private MeetingRoomDTO toRoomDTO(MeetingRoom room) {
        MeetingRoomDTO dto = new MeetingRoomDTO();
        dto.setId(room.getId());
        dto.setName(room.getName());
        dto.setLocation(room.getLocation());
        dto.setCapacity(room.getCapacity());
        dto.setEquipment(room.getEquipment());
        dto.setEnabled(room.getEnabled());
        return dto;
    }

    private MeetingReservationDTO toReservationDTO(MeetingReservation r) {
        MeetingReservationDTO dto = new MeetingReservationDTO();
        dto.setId(r.getId());
        dto.setTitle(r.getTitle());
        dto.setDescription(r.getDescription());
        dto.setRoomId(r.getRoomId());
        dto.setRoomName(r.getRoomName());
        dto.setStartTime(r.getStartTime());
        dto.setEndTime(r.getEndTime());
        dto.setOrganizerId(r.getOrganizerId());
        dto.setOrganizerName(r.getOrganizerName());
        dto.setStatus(r.getStatus());
        dto.setCheckinCode(r.getCheckinCode());
        dto.setCheckinCount(r.getCheckinCount());
        dto.setMinutes(r.getMinutes());
        
        // 获取参会人
        List<MeetingAttendee> attendees = attendeeRepository.findByReservationId(r.getId());
        dto.setAttendeeCount(attendees.size());
        
        return dto;
    }
}
