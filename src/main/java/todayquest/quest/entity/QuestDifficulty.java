package todayquest.quest.entity;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public enum QuestDifficulty {

    veryEasy(1, 10, "매우 쉬움"),
    easy(2, 20, "쉬움"),
    normal(3, 30, "보통"),
    hard(4, 40, "어려움"),
    veryHard(5, 50, "매우 어려움");

    int experience;
    int gold;
    String text;

    QuestDifficulty(int experience, int gold, String text) {
        this.experience = experience;
        this.gold = gold;
        this.text = text;
    }

    public static List<QuestDifficulty> getEnumList() {
        return Arrays.stream(QuestDifficulty.values())
                .collect(Collectors.toList());
    }
}
