package com.project.cbsbackend.repository;

import com.project.cbsbackend.entity.ApiRoleLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ApiRoleLinkRepository extends JpaRepository<ApiRoleLink, Long> {

    // Get all active API permissions for a list of role IDs
    @Query("SELECT a FROM ApiRoleLink a WHERE a.role.id IN :roleIds AND a.isActive = true AND a.isDeleted = false")
    List<ApiRoleLink> findActiveByRoleIds(@Param("roleIds") List<Long> roleIds);
}