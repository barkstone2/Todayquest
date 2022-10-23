package todayquest.user.dto;

import lombok.Data;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
public class UserRequestDto {

    @Pattern(regexp = "^([a-zA-Z0-9가-힣].)[a-zA-Z0-9가-힣\\s]*$", message = "{nickname.pattern}")
    @Size(max = 20, message = "{nickname.size}")
    private String nickname;
}
