package todayquest.reward.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import todayquest.reward.entity.Reward;
import todayquest.reward.entity.RewardGrade;
import todayquest.user.entity.UserInfo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


@Data
@NoArgsConstructor
public class RewardRequestDto {

    private Long id;
    @NotBlank @Size(max = 30)
    private String name;
    @NotBlank @Size(max = 100)
    private String description;
    @NotNull
    private RewardGrade grade;

    public Reward mapToEntity(UserInfo userInfo) {

        return Reward.builder()
                .user(userInfo)
                .grade(this.grade)
                .description(this.description)
                .name(this.name)
                .build();
    }

    @Builder
    public RewardRequestDto(String name, String description, RewardGrade grade) {
        this.name = name;
        this.description = description;
        this.grade = grade;
    }
}
