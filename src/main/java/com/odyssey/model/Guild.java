package com.odyssey.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "guilds")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Guild {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 3, max = 50)
    @Column(unique = true)
    private String name;

    @Size(max = 300)
    private String description;

    @Size(max = 100)
    private String motto;

    private String emblem;

    @OneToMany(mappedBy = "guild", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<User> members = new ArrayList<>();

    public int getTotalXp() {
        return members.stream().mapToInt(User::getXp).sum();
    }

    public int getMemberCount() {
        return members.size();
    }
}
