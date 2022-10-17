package todayquest.reward.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import todayquest.reward.entity.Reward;
import todayquest.reward.entity.RewardGrade;
import todayquest.user.entity.UserInfo;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RewardRequestDto {

    private Long id;
    private String name;
    private String description;
    private RewardGrade grade;

    public Reward mapToEntity(UserInfo userInfo) {
        return Reward.builder()
                .user(userInfo)
                .grade(this.grade)
                .description(this.description)
                .name(this.name)
                .build();
    }
}
