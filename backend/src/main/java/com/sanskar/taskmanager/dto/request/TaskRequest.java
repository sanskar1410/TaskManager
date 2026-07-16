package com.sanskar.taskmanager.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class TaskRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotBlank(message = "Assignee email is required")
    @Email(message = "Assignee email must be valid")
    private String assigneeEmail;

    private LocalDate dueDate;
}
