package com.odyssey.repository;

import com.odyssey.model.Achievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AchievementRepository extends JpaRepository<Achievement, Long> {
    List<Achievement> findByRarity(Achievement.Rarity rarity);
    List<Achievement> findByXpRequiredLessThanEqual(int xp);
}
