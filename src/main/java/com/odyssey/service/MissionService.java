package com.odyssey.service;

import com.odyssey.model.Mission;
import com.odyssey.model.Quest;
import com.odyssey.repository.MissionRepository;
import com.odyssey.repository.QuestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MissionService {

    private final MissionRepository missionRepository;
    private final QuestRepository questRepository;
    private final UserService userService;

    public Mission create(Long questId, Mission mission) {
        Quest quest = questRepository.findById(questId)
            .orElseThrow(() -> new RuntimeException("Quest not found: " + questId));
        mission.setQuest(quest);
        Mission saved = missionRepository.save(mission);
        quest.recalculateProgress();
        questRepository.save(quest);
        return saved;
    }

    public List<Mission> findByQuestId(Long questId) {
        return missionRepository.findByQuestIdOrderByOrderIndex(questId);
    }

    public Mission findById(Long id) {
        return missionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Mission not found: " + id));
    }

    @Transactional
    public Mission toggleComplete(Long id) {
        Mission mission = findById(id);
        mission.setCompleted(!mission.isCompleted());
        Mission saved = missionRepository.save(mission);

        Quest quest = mission.getQuest();
        quest.getMissions();
        quest.recalculateProgress();

        if (quest.getProgress() == 100) {
            quest.setStatus(Quest.Status.COMPLETED);
            quest.setCompletedAt(java.time.LocalDate.now());
            userService.addXp(quest.getUser().getId(), quest.getXpReward());
        }
        questRepository.save(quest);
        return saved;
    }

    public void delete(Long id) {
        Mission mission = findById(id);
        Quest quest = mission.getQuest();
        missionRepository.deleteById(id);
        quest.getMissions().remove(mission);
        quest.recalculateProgress();
        questRepository.save(quest);
    }
}
