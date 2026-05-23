package com.odyssey.service;

import com.odyssey.model.Guild;
import com.odyssey.model.User;
import com.odyssey.repository.GuildRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GuildService {

    private final GuildRepository guildRepository;
    private final UserService userService;

    public Guild create(Guild guild) {
        if (guildRepository.existsByName(guild.getName()))
            throw new RuntimeException("Guild name already taken: " + guild.getName());
        return guildRepository.save(guild);
    }

    public List<Guild> findAll() {
        return guildRepository.findAll();
    }

    public Guild findById(Long id) {
        return guildRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Guild not found: " + id));
    }

    public Guild update(Long id, Guild updated) {
        Guild existing = findById(id);
        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());
        existing.setMotto(updated.getMotto());
        existing.setEmblem(updated.getEmblem());
        return guildRepository.save(existing);
    }

    @Transactional
    public Guild addMember(Long guildId, Long userId) {
        Guild guild = findById(guildId);
        userService.joinGuild(userId, guild);
        return findById(guildId);
    }

    @Transactional
    public void removeMember(Long guildId, Long userId) {
        findById(guildId);
        userService.leaveGuild(userId);
    }

    public List<User> getMembers(Long guildId) {
        return findById(guildId).getMembers();
    }

    public void delete(Long id) {
        Guild guild = findById(id);
        guild.getMembers().forEach(u -> u.setGuild(null));
        guildRepository.deleteById(id);
    }
}
