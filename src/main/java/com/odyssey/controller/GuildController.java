package com.odyssey.controller;

import com.odyssey.model.Guild;
import com.odyssey.model.User;
import com.odyssey.service.GuildService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/guilds")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class GuildController {

    private final GuildService guildService;

    @PostMapping
    public ResponseEntity<Guild> create(@Valid @RequestBody Guild guild) {
        return ResponseEntity.status(201).body(guildService.create(guild));
    }

    @GetMapping
    public ResponseEntity<List<Guild>> getAll() {
        return ResponseEntity.ok(guildService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Guild> getById(@PathVariable Long id) {
        return ResponseEntity.ok(guildService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Guild> update(@PathVariable Long id, @Valid @RequestBody Guild guild) {
        return ResponseEntity.ok(guildService.update(id, guild));
    }

    @PostMapping("/{id}/members/{userId}")
    public ResponseEntity<Guild> addMember(@PathVariable Long id, @PathVariable Long userId) {
        return ResponseEntity.ok(guildService.addMember(id, userId));
    }

    @DeleteMapping("/{id}/members/{userId}")
    public ResponseEntity<Void> removeMember(@PathVariable Long id, @PathVariable Long userId) {
        guildService.removeMember(id, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/members")
    public ResponseEntity<List<User>> getMembers(@PathVariable Long id) {
        return ResponseEntity.ok(guildService.getMembers(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        guildService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
