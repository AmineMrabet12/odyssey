package com.odyssey.config;

import com.odyssey.model.*;
import com.odyssey.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CharacterProfileRepository profileRepository;
    private final QuestRepository questRepository;
    private final MissionRepository missionRepository;
    private final CategoryRepository categoryRepository;
    private final GuildRepository guildRepository;
    private final AchievementRepository achievementRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) return;
        log.info("Seeding Odyssey database...");

        // Categories
        Category health = categoryRepository.save(Category.builder().name("Health & Fitness").icon("💪").color("#e74c3c").build());
        Category learning = categoryRepository.save(Category.builder().name("Learning").icon("📚").color("#3498db").build());
        Category career = categoryRepository.save(Category.builder().name("Career").icon("💼").color("#f39c12").build());
        Category personal = categoryRepository.save(Category.builder().name("Personal Growth").icon("🌱").color("#27ae60").build());
        Category social = categoryRepository.save(Category.builder().name("Social").icon("👥").color("#9b59b6").build());

        // Achievements
        achievementRepository.saveAll(List.of(
            Achievement.builder().name("First Step").description("Complete your first quest").icon("👣").xpRequired(0).rarity(Achievement.Rarity.COMMON).build(),
            Achievement.builder().name("Apprentice").description("Reach 500 XP").icon("⚔️").xpRequired(500).rarity(Achievement.Rarity.COMMON).build(),
            Achievement.builder().name("Warrior").description("Reach 1000 XP").icon("🛡️").xpRequired(1000).rarity(Achievement.Rarity.RARE).build(),
            Achievement.builder().name("Champion").description("Reach 2500 XP").icon("🏆").xpRequired(2500).rarity(Achievement.Rarity.RARE).build(),
            Achievement.builder().name("Legend").description("Reach 5000 XP").icon("⭐").xpRequired(5000).rarity(Achievement.Rarity.EPIC).build(),
            Achievement.builder().name("Code Wizard").description("Reach 7500 XP").icon("🧙").xpRequired(7500).rarity(Achievement.Rarity.EPIC).build(),
            Achievement.builder().name("Dragon Slayer").description("Reach 10000 XP").icon("🐉").xpRequired(10000).rarity(Achievement.Rarity.LEGENDARY).build(),
            Achievement.builder().name("The Unstoppable").description("Reach 15000 XP").icon("💎").xpRequired(15000).rarity(Achievement.Rarity.LEGENDARY).build()
        ));

        // Guild
        Guild guild = guildRepository.save(Guild.builder()
            .name("The Spring Boot Knights")
            .description("An elite guild of backend warriors who craft APIs with precision and honor.")
            .motto("Code with purpose, ship with glory.")
            .emblem("⚔️")
            .build());

        // Demo User 1
        User hero1 = userRepository.save(User.builder()
            .username("DragonSlayer")
            .email("dragon@odyssey.com")
            .password("secret123")
            .xp(2750)
            .level(User.computeLevel(2750))
            .guild(guild)
            .build());

        profileRepository.save(CharacterProfile.builder()
            .user(hero1)
            .fullName("Mohamed Amine")
            .avatarClass(CharacterProfile.AvatarClass.MAGE)
            .bio("A legendary Spring Boot developer on a quest for clean architecture and zero bugs.")
            .title("Master of APIs")
            .build());

        // Demo User 2
        User hero2 = userRepository.save(User.builder()
            .username("IronCoder")
            .email("iron@odyssey.com")
            .password("secret123")
            .xp(1200)
            .level(User.computeLevel(1200))
            .guild(guild)
            .build());

        profileRepository.save(CharacterProfile.builder()
            .user(hero2)
            .fullName("Sami Ben")
            .avatarClass(CharacterProfile.AvatarClass.WARRIOR)
            .bio("Full-stack warrior who fears no stack trace.")
            .title("The Debugger")
            .build());

        // Demo User 3
        User hero3 = userRepository.save(User.builder()
            .username("NightOwl")
            .email("night@odyssey.com")
            .password("secret123")
            .xp(400)
            .level(User.computeLevel(400))
            .build());

        profileRepository.save(CharacterProfile.builder()
            .user(hero3)
            .fullName("Rania Kh")
            .avatarClass(CharacterProfile.AvatarClass.ROGUE)
            .bio("Silent but deadly. She ships code at 3am and wins.")
            .title("Shadow Dev")
            .build());

        // Quests for hero1
        Quest q1 = questRepository.save(Quest.builder()
            .title("Master Spring Boot")
            .description("Complete all Spring Boot labs and build a production-ready project.")
            .difficulty(Quest.Difficulty.LEGENDARY)
            .status(Quest.Status.ACTIVE)
            .xpReward(500)
            .progress(60)
            .user(hero1)
            .categories(new java.util.HashSet<>(List.of(learning, career)))
            .build());

        missionRepository.saveAll(List.of(
            Mission.builder().title("Complete dependency injection lab").completed(true).orderIndex(1).quest(q1).build(),
            Mission.builder().title("Build RESTful API with CRUD").completed(true).orderIndex(2).quest(q1).build(),
            Mission.builder().title("Implement JPA relationships").completed(true).orderIndex(3).quest(q1).build(),
            Mission.builder().title("Add Bean Validation").completed(false).orderIndex(4).quest(q1).build(),
            Mission.builder().title("Deploy to production").completed(false).orderIndex(5).quest(q1).build()
        ));

        Quest q2 = questRepository.save(Quest.builder()
            .title("Run 5km Without Stopping")
            .description("Build the endurance to run 5km in under 30 minutes.")
            .difficulty(Quest.Difficulty.HARD)
            .status(Quest.Status.ACTIVE)
            .xpReward(300)
            .progress(33)
            .user(hero1)
            .categories(new java.util.HashSet<>(List.of(health)))
            .build());

        missionRepository.saveAll(List.of(
            Mission.builder().title("Run 1km without stopping").completed(true).orderIndex(1).quest(q2).build(),
            Mission.builder().title("Run 2km without stopping").completed(false).orderIndex(2).quest(q2).build(),
            Mission.builder().title("Run 5km without stopping").completed(false).orderIndex(3).quest(q2).build()
        ));

        Quest q3 = questRepository.save(Quest.builder()
            .title("Read Clean Code by Robert Martin")
            .description("Read all 17 chapters and take notes on each principle.")
            .difficulty(Quest.Difficulty.MEDIUM)
            .status(Quest.Status.COMPLETED)
            .xpReward(200)
            .progress(100)
            .completedAt(LocalDate.now().minusDays(5))
            .user(hero1)
            .categories(new java.util.HashSet<>(List.of(learning, personal)))
            .build());

        // Quests for hero2
        Quest q4 = questRepository.save(Quest.builder()
            .title("Learn React in 30 Days")
            .description("Follow a structured plan to master React fundamentals.")
            .difficulty(Quest.Difficulty.HARD)
            .status(Quest.Status.ACTIVE)
            .xpReward(350)
            .progress(45)
            .user(hero2)
            .categories(new java.util.HashSet<>(List.of(learning, career)))
            .build());

        missionRepository.saveAll(List.of(
            Mission.builder().title("Complete React basics tutorial").completed(true).orderIndex(1).quest(q4).build(),
            Mission.builder().title("Build a todo app in React").completed(true).orderIndex(2).quest(q4).build(),
            Mission.builder().title("Learn React Router").completed(false).orderIndex(3).quest(q4).build(),
            Mission.builder().title("Learn state management (Redux)").completed(false).orderIndex(4).quest(q4).build()
        ));

        Quest q5 = questRepository.save(Quest.builder()
            .title("Network with 10 developers")
            .description("Attend meetups or reach out to senior devs for mentorship.")
            .difficulty(Quest.Difficulty.EASY)
            .status(Quest.Status.ACTIVE)
            .xpReward(150)
            .progress(20)
            .user(hero3)
            .categories(new java.util.HashSet<>(List.of(social, career)))
            .build());

        // Auto-unlock achievements based on XP
        for (User u : List.of(hero1, hero2, hero3)) {
            var eligible = achievementRepository.findByXpRequiredLessThanEqual(u.getXp());
            u.setAchievements(new java.util.HashSet<>(eligible));
            userRepository.save(u);
        }

        log.info("Odyssey database seeded successfully. Adventure begins!");
    }
}
