package com.odyssey.repository;

import com.odyssey.model.Mission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MissionRepository extends JpaRepository<Mission, Long> {
    List<Mission> findByQuestIdOrderByOrderIndex(Long questId);
    long countByQuestIdAndCompleted(Long questId, boolean completed);
}
