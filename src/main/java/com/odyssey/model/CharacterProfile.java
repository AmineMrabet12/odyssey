package com.odyssey.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "character_profiles")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class CharacterProfile {

    public enum AvatarClass { WARRIOR, MAGE, ROGUE, PALADIN }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String fullName;

    @Enumerated(EnumType.STRING)
    private AvatarClass avatarClass;

    @Size(max = 200)
    private String bio;

    private String title;

    private String phone;

    private String address;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"profile", "quests", "achievements", "guild"})
    private User user;
}
