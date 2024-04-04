package dailyquest.user.entity

import java.util.*

enum class RoleType(val code: String, val displayName: String) {
    USER("ROLE_USER", "일반 사용자 권한"),
    ADMIN("ROLE_ADMIN", "관리자 권한"),
    GUEST("GUEST", "게스트 권한");

    companion object {
        @JvmStatic
        fun of(code: String): RoleType {
            return Arrays.stream(values())
                .filter { r: RoleType -> r.code == code }
                .findAny()
                .orElse(GUEST)
        }
    }
}