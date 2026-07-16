package com.sanskar.taskmanager.repository;

import com.sanskar.taskmanager.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {
}
