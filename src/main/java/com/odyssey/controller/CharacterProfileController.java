package com.odyssey.controller;

import com.odyssey.model.CharacterProfile;
import com.odyssey.service.CharacterProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CharacterProfileController {

    private final CharacterProfileService profileService;

    @PostMapping
    public ResponseEntity<CharacterProfile> create(
            @RequestParam Long userId,
            @Valid @RequestBody CharacterProfile profile) {
        return ResponseEntity.status(201).body(profileService.create(userId, profile));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<CharacterProfile> getByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(profileService.findByUserId(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CharacterProfile> getById(@PathVariable Long id) {
        return ResponseEntity.ok(profileService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CharacterProfile> update(
            @PathVariable Long id,
            @Valid @RequestBody CharacterProfile profile) {
        return ResponseEntity.ok(profileService.update(id, profile));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        profileService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
