package todayquest.achievement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import todayquest.achievement.entity.Achievement;
import todayquest.achievement.entity.AchievementType;

import java.util.List;

public interface AchievementRepository extends JpaRepository<Achievement, Long> {
    List<Achievement> findByType(AchievementType type);
}
