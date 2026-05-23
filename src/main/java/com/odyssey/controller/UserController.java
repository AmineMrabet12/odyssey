package com.odyssey.controller;

import com.odyssey.model.Achievement;
import com.odyssey.model.User;
import com.odyssey.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;

    @Value("${app.message}")
    private String appMessage;

    @GetMapping("/message")
    public ResponseEntity<String> message() {
        return ResponseEntity.ok(appMessage);
    }

    @PostMapping
    public ResponseEntity<User> create(@Valid @RequestBody User user) {
        return ResponseEntity.status(201).body(userService.save(user));
    }

    @GetMapping
    public ResponseEntity<List<User>> getAll() {
        return ResponseEntity.ok(userService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> update(@PathVariable Long id, @Valid @RequestBody User user) {
        return ResponseEntity.ok(userService.update(id, user));
    }

    @PostMapping("/{id}/xp")
    public ResponseEntity<User> addXp(@PathVariable Long id, @RequestParam int amount) {
        return ResponseEntity.ok(userService.addXp(id, amount));
    }

    @GetMapping("/{id}/achievements")
    public ResponseEntity<Set<Achievement>> getAchievements(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getAchievements(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
