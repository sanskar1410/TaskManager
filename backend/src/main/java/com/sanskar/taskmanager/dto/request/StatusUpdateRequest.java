package com.sanskar.taskmanager.dto.request;

import com.sanskar.taskmanager.entity.TaskStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StatusUpdateRequest {

    @NotNull(message = "Status is required")
    private TaskStatus status;
}
