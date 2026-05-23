package com.odyssey.controller;

import com.odyssey.model.Quest;
import com.odyssey.service.QuestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quests")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class QuestController {

    private final QuestService questService;

    @PostMapping
    public ResponseEntity<Quest> create(
            @RequestParam Long userId,
            @Valid @RequestBody Quest quest) {
        return ResponseEntity.status(201).body(questService.create(userId, quest));
    }

    @GetMapping
    public ResponseEntity<List<Quest>> getAll() {
        return ResponseEntity.ok(questService.findAll());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Quest>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(questService.findByUserId(userId));
    }

    @GetMapping("/user/{userId}/status/{status}")
    public ResponseEntity<List<Quest>> getByUserAndStatus(
            @PathVariable Long userId,
            @PathVariable Quest.Status status) {
        return ResponseEntity.ok(questService.findByUserIdAndStatus(userId, status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Quest> getById(@PathVariable Long id) {
        return ResponseEntity.ok(questService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Quest> update(@PathVariable Long id, @Valid @RequestBody Quest quest) {
        return ResponseEntity.ok(questService.update(id, quest));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Quest> updateStatus(
            @PathVariable Long id,
            @RequestParam Quest.Status status) {
        return ResponseEntity.ok(questService.updateStatus(id, status));
    }

    @PostMapping("/{id}/categories/{categoryId}")
    public ResponseEntity<Quest> addCategory(@PathVariable Long id, @PathVariable Long categoryId) {
        return ResponseEntity.ok(questService.addCategory(id, categoryId));
    }

    @DeleteMapping("/{id}/categories/{categoryId}")
    public ResponseEntity<Quest> removeCategory(@PathVariable Long id, @PathVariable Long categoryId) {
        return ResponseEntity.ok(questService.removeCategory(id, categoryId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        questService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
