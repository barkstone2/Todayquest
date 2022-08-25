package todayquest.user.entity;

import lombok.Getter;

@Getter
public enum DifficultyType {
    level("레벨"), difficulty("난이도");
    String typeText;

    DifficultyType(String typeText) {
        this.typeText = typeText;
    }
}
