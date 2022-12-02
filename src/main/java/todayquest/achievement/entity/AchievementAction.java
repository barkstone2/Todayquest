package todayquest.achievement.entity;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static todayquest.achievement.entity.AchievementType.*;

public enum AchievementAction {

    PROCEED("진행", QUEST),
    COMPLETE("완료", QUEST),
    DISCARD("포기", QUEST),
    FAIL("실패", QUEST),
    DELETE("삭제", QUEST),

    EARN("획득", ITEM),
    USE("사용", ITEM),
    ABANDON("버림", ITEM),

    GET_GOLD("획득", GOLD),

    GET_EXP("획득", EXPERIENCE);

    private String message;
    private AchievementType type;

    AchievementAction(String message, AchievementType type) {
        this.message = message;
        this.type = type;
    }

    public static List<AchievementAction> getEnumListOfType(AchievementType type) {
        return Arrays.stream(AchievementAction.values())
                .filter(a -> a.type.equals(type))
                .collect(Collectors.toList());
    }
}
