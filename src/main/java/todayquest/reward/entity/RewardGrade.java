package todayquest.reward.entity;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public enum RewardGrade {
    F("F등급", "#9e9e9e"),
    E("E등급", "#c5fdb8"),

    D("D등급", "#fcf7ba"),
    C("C등급", "#3986ff"),

    B("B등급", "#a056eb"),
    A("A등급", "#fea552"),
    S("S등급", "#f23f3f");

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
