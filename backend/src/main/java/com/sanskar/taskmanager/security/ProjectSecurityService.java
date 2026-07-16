package com.sanskar.taskmanager.security;

import com.sanskar.taskmanager.entity.ProjectRole;
import com.sanskar.taskmanager.repository.ProjectMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("projectSecurity")
@RequiredArgsConstructor
public class ProjectSecurityService {

    private final ProjectMemberRepository projectMemberRepository;

    public boolean isProjectAdmin(Long projectId, Authentication authentication) {
        String email = authentication.getName();
        return projectMemberRepository.findByProjectIdAndUserEmail(projectId, email)
                .map(pm -> pm.getRole() == ProjectRole.ADMIN)
                .orElse(false);
    }

    public boolean isProjectMember(Long projectId, Authentication authentication) {
        String email = authentication.getName();
        return projectMemberRepository.existsByProjectIdAndUserEmail(projectId, email);
    }
}
