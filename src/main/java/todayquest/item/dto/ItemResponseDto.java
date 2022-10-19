package todayquest.item.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import todayquest.item.entity.Item;
import todayquest.reward.entity.Reward;
import todayquest.reward.entity.RewardGrade;

@Builder
@AllArgsConstructor
@Data
public class ItemResponseDto {

    private Long id;
    private String name;
    private int count;
    private String description;
    private RewardGrade grade;

    public static ItemResponseDto createDto(Item item) {
        Reward reward = item.getReward();
        return ItemResponseDto.builder()
                .id(item.getId())
                .name(reward.getName())
                .description(reward.getDescription())
                .grade(reward.getGrade())
                .count(item.getCount())
                .build();
    }

}
