package todayquest.user.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRequestDto {

    /** 영어 대소문자, 숫자, 한글 허용 & 한글 자모 비허용 & 시작과 끝을 제외한 위치에 공백 허용 */
    @Pattern(regexp = "^([a-zA-Z0-9가-힣[^\\sㄱ-ㅎㅏ-ㅡ]]+)([a-zA-Z0-9가-힣\\s]*)([a-zA-Z0-9가-힣[^\\sㄱ-ㅎㅏ-ㅡ]]+)$", message = "{nickname.pattern}")
    @Size(max = 20, message = "{nickname.size}")
    private String nickname;

    private Integer resetTime;
    private Integer coreTime;

    public String getNickname() {
        return nickname;
    }

    public Integer getResetTime() {
        return resetTime;
    }

    public Integer getCoreTime() {
        return coreTime;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public UserRequestDto() {
    }

    public UserRequestDto(Integer resetTime, Integer coreTime) {
        this.resetTime = resetTime;
        this.coreTime = coreTime;
    }
}
