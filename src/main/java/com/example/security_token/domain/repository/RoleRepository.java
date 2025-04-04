package com.example.security_token.domain.repository;

import com.example.security_token.domain.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);

    boolean existsByNameIgnoreCase(String name);


    Set<Role> findByNameIn(Collection<String> names);
}
