package todayquest.achievement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import todayquest.achievement.entity.UserAchievement;

import java.util.List;

public interface UserAchievementRepository extends JpaRepository<UserAchievement, Long> {
    List<UserAchievement> findByUserId(Long userId);
}
