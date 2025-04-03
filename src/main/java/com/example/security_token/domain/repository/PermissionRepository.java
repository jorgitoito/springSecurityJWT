package com.example.security_token.domain.repository;

import com.example.security_token.domain.model.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
    Optional<Permission> findByName(String name);

    Set<Permission> findByNameIn(Collection<String> names);
}
