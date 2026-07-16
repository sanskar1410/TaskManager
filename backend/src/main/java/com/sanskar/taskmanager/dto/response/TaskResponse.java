package com.sanskar.taskmanager.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class TaskResponse {
    private Long id;
    private String title;
    private String description;
    private String status;
    private LocalDate dueDate;
    private String assigneeName;
    private String assigneeEmail;
    private Long projectId;
    private boolean overdue;
}
