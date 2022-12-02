package todayquest.achievement.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import todayquest.achievement.entity.Achievement;
import todayquest.achievement.entity.AchievementType;
import todayquest.achievement.entity.UserAchievement;
import todayquest.achievement.repository.AchievementRepository;
import todayquest.achievement.repository.UserAchievementRepository;
import todayquest.item.repository.ItemLogRepository;
import todayquest.quest.repository.QuestLogRepository;
import todayquest.user.entity.UserInfo;
import todayquest.user.repository.UserRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class AchievementService {

    private final AchievementRepository achievementRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final UserRepository userRepository;
    private final QuestLogRepository questLogRepository;
    private final ItemLogRepository itemLogRepository;

    public void checkAndAttainQuestAchievement(Long userId) {
        Map<String, Long> analytics = questLogRepository.getQuestAnalytics(userId);
        checkAndAttainAchievement(userId, AchievementType.QUEST, analytics);
    }

    public void checkAndAttainItemAchievement(Long userId) {
        Map<String, Long> analytics = itemLogRepository.getItemAnalytics(userId);
        checkAndAttainAchievement(userId, AchievementType.ITEM, analytics);
    }

    private void checkAndAttainAchievement(Long userId, AchievementType type, Map<String, Long> analytics) {
        List<UserAchievement> userAchievements = userAchievementRepository.findByUserId(userId);

        List<Long> attainedAchievementIds = userAchievements.stream()
                .map(UserAchievement::getAchievementId)
                .collect(Collectors.toList());

        List<Achievement> achievements = achievementRepository.findByType(type);
        UserInfo userInfo = userRepository.getById(userId);

        List<Achievement> notAttainedAchievements = achievements.stream()
                .filter(achievement -> !attainedAchievementIds.contains(achievement.getId()))
                .collect(Collectors.toList());

        List<UserAchievement> newlyAttainedAchievements = notAttainedAchievements.stream()
                .filter(achievement -> achievement.getTargetNumber() <= analytics.get(achievement.getAction().name()))
                .map(achievement -> new UserAchievement(userInfo, achievement))
                .collect(Collectors.toList());

        userAchievementRepository.saveAll(newlyAttainedAchievements);
    }

}
