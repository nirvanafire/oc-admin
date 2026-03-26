package com.nirvanafire.ocadmin.service;

import com.nirvanafire.ocadmin.dto.MeetingReservationDTO;
import com.nirvanafire.ocadmin.dto.MeetingRoomDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 会议服务接口
 */
public interface MeetingService {
    
    // ==================== 会议室 ====================
    
    /**
     * 获取会议室列表
     */
    List<MeetingRoomDTO> getRooms();
    
    /**
     * 创建会议室
     */
    MeetingRoomDTO createRoom(Long userId, MeetingRoomDTO dto);
    
    /**
     * 更新会议室
     */
    MeetingRoomDTO updateRoom(Long userId, Long roomId, MeetingRoomDTO dto);
    
    /**
     * 删除会议室
     */
    void deleteRoom(Long userId, Long roomId);
    
    /**
     * 获取可用会议室
     */
    List<MeetingRoomDTO> getAvailableRooms(java.time.LocalDateTime startTime, java.time.LocalDateTime endTime);
    
    // ==================== 会议预约 ====================
    
    /**
     * 创建会议预约
     */
    MeetingReservationDTO createReservation(Long userId, String username, MeetingReservationDTO dto);
    
    /**
     * 获取我的会议列表
     */
    Page<MeetingReservationDTO> getMyReservations(Long userId, Pageable pageable);
    
    /**
     * 获取会议详情
     */
    MeetingReservationDTO getReservation(Long reservationId);
    
    /**
     * 取消会议
     */
    void cancelReservation(Long userId, Long reservationId);
    
    /**
     * 签到
     */
    void checkin(Long userId, Long reservationId);
    
    /**
     * 添加会议纪要
     */
    MeetingReservationDTO addMinutes(Long userId, Long reservationId, String minutes, String attachmentUrls);
    
    // ==================== 统计 ====================
    
    /**
     * 获取会议室使用统计
     */
    Object getRoomStatistics(Long roomId, int year, int month);
}
