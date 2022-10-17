package todayquest.reward.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import todayquest.reward.entity.Reward;
import todayquest.reward.entity.RewardGrade;

@Data
@AllArgsConstructor @Builder
public class RewardResponseDto {
    private Long id;
    private String name;
    private String description;
    private RewardGrade grade;

    public static RewardResponseDto createDto(Reward reward) {
        return RewardResponseDto.builder()
                .id(reward.getId())
                .name(reward.getName())
                .description(reward.getDescription())
                .grade(reward.getGrade())
                .build();
    }
}
