package todayquest.quest.entity;

import lombok.Getter;
import todayquest.user.dto.UserPrincipal;
import todayquest.user.entity.DifficultyType;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static todayquest.user.entity.DifficultyType.*;

@Getter
public enum QuestDifficulty {

    veryEasy(1, 10, difficulty, "매우 쉬움"),
    easy(2, 20, difficulty, "쉬움"),
    normal(3, 30, difficulty, "보통"),
    hard(4, 40, difficulty, "어려움"),
    veryHard(5, 50, difficulty, "매우 어려움"),

    l1(1, 10, level, "Level 1"),
    l2(1, 10, level, "Level 2"),
    l3(2, 20, level, "Level 3"),
    l4(2, 20, level, "Level 4"),
    l5(3, 30, level, "Level 5"),
    l6(3, 30, level, "Level 6"),
    l7(4, 40, level, "Level 7"),
    l8(4, 40, level, "Level 8"),
    l9(5, 50, level, "Level 9"),
    l10(5, 50, level, "Level 10");

    int experience;
    int gold;
    DifficultyType type;
    String text;

    QuestDifficulty(int experience, int gold, DifficultyType type, String text) {
        this.experience = experience;
        this.gold = gold;
        this.type = type;
        this.text = text;
    }

    public static List<QuestDifficulty> getEnumListOfType(DifficultyType type) {
        return Arrays.stream(QuestDifficulty.values())
                .filter(e -> e.type.equals(type))
                .collect(Collectors.toList());
    }
}
