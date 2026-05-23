package com.odyssey.service;

import com.odyssey.model.Achievement;
import com.odyssey.model.User;
import com.odyssey.repository.AchievementRepository;
import com.odyssey.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final AchievementRepository achievementRepository;

    public User save(User user) {
        if (userRepository.existsByEmail(user.getEmail()))
            throw new RuntimeException("Email already in use");
        if (userRepository.existsByUsername(user.getUsername()))
            throw new RuntimeException("Username already taken");
        return userRepository.save(user);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User findById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Hero not found with id: " + id));
    }

    public User update(Long id, User updated) {
        User existing = findById(id);
        existing.setUsername(updated.getUsername());
        existing.setEmail(updated.getEmail());
        return userRepository.save(existing);
    }

    public void delete(Long id) {
        findById(id);
        userRepository.deleteById(id);
    }

    @Transactional
    public User addXp(Long userId, int amount) {
        User user = findById(userId);
        user.addXp(amount);
        User saved = userRepository.save(user);
        unlockEligibleAchievements(saved);
        return saved;
    }

    @Transactional
    public User joinGuild(Long userId, com.odyssey.model.Guild guild) {
        User user = findById(userId);
        user.setGuild(guild);
        return userRepository.save(user);
    }

    @Transactional
    public User leaveGuild(Long userId) {
        User user = findById(userId);
        user.setGuild(null);
        return userRepository.save(user);
    }

    private void unlockEligibleAchievements(User user) {
        List<Achievement> eligible = achievementRepository.findByXpRequiredLessThanEqual(user.getXp());
        Set<Achievement> current = user.getAchievements();
        eligible.stream()
            .filter(a -> !current.contains(a))
            .forEach(current::add);
        userRepository.save(user);
    }

    public Set<Achievement> getAchievements(Long userId) {
        return findById(userId).getAchievements();
    }
}
