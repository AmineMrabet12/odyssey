package com.odyssey.controller;

import com.odyssey.model.Achievement;
import com.odyssey.service.AchievementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/achievements")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AchievementController {

    private final AchievementService achievementService;

    @PostMapping
    public ResponseEntity<Achievement> create(@Valid @RequestBody Achievement achievement) {
        return ResponseEntity.status(201).body(achievementService.save(achievement));
    }

    @GetMapping
    public ResponseEntity<List<Achievement>> getAll() {
        return ResponseEntity.ok(achievementService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Achievement> getById(@PathVariable Long id) {
        return ResponseEntity.ok(achievementService.findById(id));
    }

    @GetMapping("/rarity/{rarity}")
    public ResponseEntity<List<Achievement>> getByRarity(@PathVariable Achievement.Rarity rarity) {
        return ResponseEntity.ok(achievementService.findByRarity(rarity));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Achievement> update(@PathVariable Long id, @Valid @RequestBody Achievement achievement) {
        return ResponseEntity.ok(achievementService.update(id, achievement));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        achievementService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
