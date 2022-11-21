package todayquest.achievement.entity;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum AchievementType {
    QUEST("퀘스트"), ITEM("아이템"), GOLD("골드"), EXPERIENCE("경험치");

    private String message;

    AchievementType(String message) {
        this.message = message;
    }

    public static List<AchievementType> getEnumList() {
        return Arrays.stream(AchievementType.values())
                .collect(Collectors.toList());
    }
}
