package dailyquest.common;

import dailyquest.user.dto.UserResponse;
import org.mockito.Mockito;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import dailyquest.annotation.WithCustomMockUser;
import dailyquest.user.dto.UserPrincipal;

import java.util.HashMap;

public class WithMockCustomUserSecurityContextFactory implements WithSecurityContextFactory<WithCustomMockUser> {

    @Override
    public SecurityContext createSecurityContext(WithCustomMockUser mockUser) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        UserResponse userResponse = new UserResponse(1, "");
        UserPrincipal principal = UserPrincipal.from(userResponse, new HashMap<>());
        UserPrincipal spyPrincipal = Mockito.spy(principal);

        Authentication auth = new UsernamePasswordAuthenticationToken(spyPrincipal, principal.getPassword(), principal.getAuthorities());
        context.setAuthentication(auth);
        return context;
    }
}
