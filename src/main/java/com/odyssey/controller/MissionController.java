package com.odyssey.controller;

import com.odyssey.model.Mission;
import com.odyssey.service.MissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/missions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MissionController {

    private final MissionService missionService;

    @PostMapping
    public ResponseEntity<Mission> create(
            @RequestParam Long questId,
            @Valid @RequestBody Mission mission) {
        return ResponseEntity.status(201).body(missionService.create(questId, mission));
    }

    @GetMapping("/quest/{questId}")
    public ResponseEntity<List<Mission>> getByQuest(@PathVariable Long questId) {
        return ResponseEntity.ok(missionService.findByQuestId(questId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Mission> getById(@PathVariable Long id) {
        return ResponseEntity.ok(missionService.findById(id));
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<Mission> toggle(@PathVariable Long id) {
        return ResponseEntity.ok(missionService.toggleComplete(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        missionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
