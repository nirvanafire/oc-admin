package com.nirvanafire.ocadmin.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 会议预约DTO
 */
@Data
public class MeetingReservationDTO {
    private Long id;
    private String title;
    private String description;
    private Long roomId;
    private String roomName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long organizerId;
    private String organizerName;
    private String status;
    private String checkinCode;
    private Integer checkinCount;
    private Integer attendeeCount;
    private String minutes;
    private String attachmentUrls;
    private List<Long> attendeeIds;
}
