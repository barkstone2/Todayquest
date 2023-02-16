package todayquest.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.util.LinkedMultiValueMap;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ErrorResponse {
    private String message;
    private HttpStatus status;
    private final LinkedMultiValueMap<String, String> errors = new LinkedMultiValueMap<>();

    public ErrorResponse(String message, HttpStatus status) {
        this.message = message;
        this.status = status;
    }
}
