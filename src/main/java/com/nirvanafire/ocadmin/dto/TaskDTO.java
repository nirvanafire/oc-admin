package com.nirvanafire.ocadmin.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class TaskDTO {
    private Long id;
    private String title;
    private String description;
    private Long creatorId;
    private String creatorName;
    private Long assigneeId;
    private String assigneeName;
    private LocalDate dueDate;
    private String priority;
    private String status;
    private String boardColumn;
}
