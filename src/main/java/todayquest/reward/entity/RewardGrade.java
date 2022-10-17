package todayquest.reward.entity;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public enum RewardGrade {
    F("F등급", "#9e9e9e"),
    E("E등급", "#cccccc"),

    D("D등급", "#4ea3ed"),
    C("C등급", "#a056eb"),

    B("B등급", "#4ea3ed"),
    A("A등급", ""),
    S("S등급", "");

    String text;
    String color;

    RewardGrade(String text, String color) {
        this.text = text;
        this.color = color;
    }

    public static List<RewardGrade> getEnumList() {
        return Arrays.stream(RewardGrade.values())
                .collect(Collectors.toList());
    }

}
