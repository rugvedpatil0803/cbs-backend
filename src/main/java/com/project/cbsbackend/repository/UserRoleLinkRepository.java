package com.project.cbsbackend.repository;

import com.project.cbsbackend.entity.User;
import com.project.cbsbackend.entity.UserRoleLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserRoleLinkRepository extends JpaRepository<UserRoleLink, Long> {

    List<UserRoleLink> findByUser(User user);
    List<UserRoleLink> findByUserId(Long userId);
    @Query("""
        SELECT url FROM UserRoleLink url
        JOIN FETCH url.user
        JOIN FETCH url.role
        WHERE url.isDeleted = false AND url.isActive = true
    """)
    List<UserRoleLink> findAllActiveRoleLinks();
}