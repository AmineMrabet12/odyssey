package com.odyssey.service;

import com.odyssey.model.Category;
import com.odyssey.model.Quest;
import com.odyssey.model.User;
import com.odyssey.repository.CategoryRepository;
import com.odyssey.repository.QuestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestService {

    private final QuestRepository questRepository;
    private final CategoryRepository categoryRepository;
    private final UserService userService;

    public Quest create(Long userId, Quest quest) {
        User user = userService.findById(userId);
        quest.setUser(user);
        return questRepository.save(quest);
    }

    public List<Quest> findAll() {
        return questRepository.findAll();
    }

    public List<Quest> findByUserId(Long userId) {
        return questRepository.findByUserId(userId);
    }

    public List<Quest> findByUserIdAndStatus(Long userId, Quest.Status status) {
        return questRepository.findByUserIdAndStatus(userId, status);
    }

    public Quest findById(Long id) {
        return questRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Quest not found: " + id));
    }

    public Quest update(Long id, Quest updated) {
        Quest existing = findById(id);
        existing.setTitle(updated.getTitle());
        existing.setDescription(updated.getDescription());
        existing.setDifficulty(updated.getDifficulty());
        existing.setXpReward(updated.getXpReward());
        return questRepository.save(existing);
    }

    @Transactional
    public Quest updateStatus(Long id, Quest.Status status) {
        Quest quest = findById(id);
        quest.setStatus(status);
        if (status == Quest.Status.COMPLETED) {
            quest.setCompletedAt(LocalDate.now());
            quest.setProgress(100);
            userService.addXp(quest.getUser().getId(), quest.getXpReward());
        }
        return questRepository.save(quest);
    }

    @Transactional
    public Quest addCategory(Long questId, Long categoryId) {
        Quest quest = findById(questId);
        Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new RuntimeException("Category not found: " + categoryId));
        quest.getCategories().add(category);
        return questRepository.save(quest);
    }

    @Transactional
    public Quest removeCategory(Long questId, Long categoryId) {
        Quest quest = findById(questId);
        quest.getCategories().removeIf(c -> c.getId().equals(categoryId));
        return questRepository.save(quest);
    }

    public void delete(Long id) {
        findById(id);
        questRepository.deleteById(id);
    }

    public long countByUserAndStatus(Long userId, Quest.Status status) {
        return questRepository.countByUserIdAndStatus(userId, status);
    }
}
