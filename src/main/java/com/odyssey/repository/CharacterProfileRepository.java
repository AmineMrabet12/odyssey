package com.odyssey.repository;

import com.odyssey.model.CharacterProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CharacterProfileRepository extends JpaRepository<CharacterProfile, Long> {
    Optional<CharacterProfile> findByUserId(Long userId);
    boolean existsByUserId(Long userId);
}
