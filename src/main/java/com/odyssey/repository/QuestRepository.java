package com.odyssey.repository;

import com.odyssey.model.Quest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestRepository extends JpaRepository<Quest, Long> {
    List<Quest> findByUserId(Long userId);
    List<Quest> findByUserIdAndStatus(Long userId, Quest.Status status);
    List<Quest> findByDifficulty(Quest.Difficulty difficulty);

    @Query("SELECT q FROM Quest q JOIN q.categories c WHERE c.name = :categoryName")
    List<Quest> findByCategoryName(String categoryName);

    long countByUserIdAndStatus(Long userId, Quest.Status status);
}
