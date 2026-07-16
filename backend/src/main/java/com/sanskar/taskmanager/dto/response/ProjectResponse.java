package com.sanskar.taskmanager.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ProjectResponse {
    private Long id;
    private String name;
    private String description;
    private String ownerName;
    private int memberCount;
    private int taskCount;
    private LocalDateTime createdAt;
    private String currentUserRole;
}
