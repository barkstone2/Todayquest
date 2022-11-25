package todayquest.achievement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import todayquest.achievement.entity.Achievement;

public interface AchievementRepository extends JpaRepository<Achievement, Long> {
}
