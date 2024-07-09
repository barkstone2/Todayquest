package dailyquest.jwt.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class SilentRefreshResult {
    private final String accessToken;
    private final String refreshToken;
}
