package todayquest.user.service;

import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import todayquest.user.dto.UserPrincipal;

public interface UserService {
    UserPrincipal processUserInfo(OidcUserRequest request, OidcUser user);
    UserPrincipal processUserInfo(OAuth2UserRequest request, OAuth2User user);
    boolean isDuplicateNickname(String nickname);
}
