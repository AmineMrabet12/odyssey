package com.odyssey.service;

import com.odyssey.model.CharacterProfile;
import com.odyssey.model.User;
import com.odyssey.repository.CharacterProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CharacterProfileService {

    private final CharacterProfileRepository profileRepository;
    private final UserService userService;

    public CharacterProfile create(Long userId, CharacterProfile profile) {
        if (profileRepository.existsByUserId(userId))
            throw new RuntimeException("Profile already exists for this hero");
        User user = userService.findById(userId);
        profile.setUser(user);
        return profileRepository.save(profile);
    }

    public CharacterProfile findByUserId(Long userId) {
        return profileRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("Profile not found for user: " + userId));
    }

    public CharacterProfile findById(Long id) {
        return profileRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Profile not found"));
    }

    public CharacterProfile update(Long id, CharacterProfile updated) {
        CharacterProfile existing = findById(id);
        existing.setFullName(updated.getFullName());
        existing.setAvatarClass(updated.getAvatarClass());
        existing.setBio(updated.getBio());
        existing.setTitle(updated.getTitle());
        existing.setPhone(updated.getPhone());
        existing.setAddress(updated.getAddress());
        return profileRepository.save(existing);
    }

    public void delete(Long id) {
        findById(id);
        profileRepository.deleteById(id);
    }
}
