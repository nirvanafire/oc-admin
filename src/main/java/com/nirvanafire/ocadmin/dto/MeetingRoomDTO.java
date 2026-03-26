package com.nirvanafire.ocadmin.dto;

import lombok.Data;

/**
 * 会议室DTO
 */
@Data
public class MeetingRoomDTO {
    private Long id;
    private String name;
    private String location;
    private Integer capacity;
    private String equipment;
    private Boolean enabled;
}
