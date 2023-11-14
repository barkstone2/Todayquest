package dailyquest.common;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import dailyquest.annotation.WithCustomMockUser;
import dailyquest.user.dto.UserPrincipal;
import dailyquest.user.entity.ProviderType;
import dailyquest.user.entity.UserInfo;

import java.util.HashMap;

public class WithMockCustomUserSecurityContextFactory implements WithSecurityContextFactory<WithCustomMockUser> {

    @Override
    public SecurityContext createSecurityContext(WithCustomMockUser user) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        UserInfo userInfo = new UserInfo("", "", ProviderType.GOOGLE);

        UserPrincipal principal = UserPrincipal.create(userInfo, new HashMap<>());

        Authentication auth = new UsernamePasswordAuthenticationToken(principal, principal.getPassword(), principal.getAuthorities());
        context.setAuthentication(auth);
        return context;
    }
}
