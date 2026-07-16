package com.sanskar.taskmanager.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProjectMemberResponse {
    private Long userId;
    private String name;
    private String email;
    private String role;
}
