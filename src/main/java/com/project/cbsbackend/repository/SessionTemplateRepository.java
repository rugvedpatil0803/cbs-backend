package com.project.cbsbackend.repository;

import com.project.cbsbackend.entity.SessionTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionTemplateRepository extends JpaRepository<SessionTemplate, Long> {
}