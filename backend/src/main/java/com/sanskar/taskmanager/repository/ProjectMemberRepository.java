package com.sanskar.taskmanager.repository;

import com.sanskar.taskmanager.entity.ProjectMember;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {

    @Query("SELECT pm FROM ProjectMember pm WHERE pm.project.id = :projectId AND pm.user.email = :email")
    Optional<ProjectMember> findByProjectIdAndUserEmail(@Param("projectId") Long projectId, @Param("email") String email);

    @Query("SELECT CASE WHEN COUNT(pm) > 0 THEN true ELSE false END FROM ProjectMember pm WHERE pm.project.id = :projectId AND pm.user.email = :email")
    boolean existsByProjectIdAndUserEmail(@Param("projectId") Long projectId, @Param("email") String email);

    List<ProjectMember> findByProjectId(Long projectId);

    List<ProjectMember> findByUserEmail(String email);
}
