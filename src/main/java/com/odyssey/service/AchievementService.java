package com.odyssey.service;

import com.odyssey.model.Achievement;
import com.odyssey.repository.AchievementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AchievementService {

    private final AchievementRepository achievementRepository;

    public Achievement save(Achievement achievement) {
        return achievementRepository.save(achievement);
    }

    public List<Achievement> findAll() {
        return achievementRepository.findAll();
    }

    public Achievement findById(Long id) {
        return achievementRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Achievement not found: " + id));
    }

    public Achievement update(Long id, Achievement updated) {
        Achievement existing = findById(id);
        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());
        existing.setIcon(updated.getIcon());
        existing.setXpRequired(updated.getXpRequired());
        existing.setRarity(updated.getRarity());
        return achievementRepository.save(existing);
    }

    public void delete(Long id) {
        findById(id);
        achievementRepository.deleteById(id);
    }

    public List<Achievement> findByRarity(Achievement.Rarity rarity) {
        return achievementRepository.findByRarity(rarity);
    }
}
