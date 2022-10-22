package todayquest.common;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import todayquest.annotation.WithCustomMockUser;
import todayquest.user.dto.UserPrincipal;
import todayquest.user.entity.UserInfo;

public class WithMockCustomUserSecurityContextFactory implements WithSecurityContextFactory<WithCustomMockUser> {

    @Override
    public SecurityContext createSecurityContext(WithCustomMockUser user) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        UserPrincipal principal = UserPrincipal.create(
                UserInfo.builder()
                        .id(user.userId())
                        .providerType(user.providerType())
                        .nickname(user.nickname())
                        .level(1)
                        .gold(0L)
                        .exp(0L)
                        .build()
        );

        Authentication auth = new UsernamePasswordAuthenticationToken(principal, principal.getPassword(), principal.getAuthorities());
        context.setAuthentication(auth);
        return context;
    }
}
