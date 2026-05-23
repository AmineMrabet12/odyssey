package com.odyssey.repository;

import com.odyssey.model.Guild;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GuildRepository extends JpaRepository<Guild, Long> {
    Optional<Guild> findByName(String name);
    boolean existsByName(String name);
}
