package todayquest.oauth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import todayquest.user.service.UserService;

@Slf4j
@RequiredArgsConstructor
@Service
public class CustomOidcUserService implements OAuth2UserService<OidcUserRequest, OidcUser> {

    private final UserService userService;

    @Override
    public OidcUser loadUser(OidcUserRequest request) throws OAuth2AuthenticationException {
        OidcUserService delegate = new OidcUserService();
        OidcUser user = delegate.loadUser(request);

        return userService.processUserInfo(request, user);
    }

}