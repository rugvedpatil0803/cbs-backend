package com.project.cbsbackend.repository;

import com.project.cbsbackend.entity.User;
import com.project.cbsbackend.entity.UserRoleLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRoleLinkRepository extends JpaRepository<UserRoleLink, Long> {

    List<UserRoleLink> findByUser(User user);
    List<UserRoleLink> findByUserId(Long userId);
}