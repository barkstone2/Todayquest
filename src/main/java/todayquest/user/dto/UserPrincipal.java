package todayquest.user.dto;

import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import todayquest.user.entity.DifficultyType;
import todayquest.user.entity.ProviderType;
import todayquest.user.entity.UserInfo;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@Getter @Setter
@RequiredArgsConstructor @Builder
@AllArgsConstructor
public class UserPrincipal implements OAuth2User, OidcUser, UserDetails {

    private final Long userId;
    private final String nickname;
    private final ProviderType providerType;
    private DifficultyType difficultyType;
    private final Collection<GrantedAuthority> authorities;

    private Map<String, Object> attributes;

    public static UserPrincipal create(UserInfo userInfo) {
        return UserPrincipal.builder()
                .userId(userInfo.getId())
                .nickname(userInfo.getNickname())
                .providerType(userInfo.getProviderType())
                .difficultyType(userInfo.getDifficultyType())
                .authorities(Collections.singletonList(new SimpleGrantedAuthority(RoleType.USER.getCode())))
                .build();
    }


    public static UserPrincipal create(UserInfo userInfo, Map<String, Object> attributes) {
        UserPrincipal userPrincipal = create(userInfo);
        userPrincipal.setAttributes(attributes);

        return userPrincipal;
    }


    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return nickname;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getName() {
        return nickname;
    }


    @Override
    public Map<String, Object> getClaims() {
        return null;
    }

    @Override
    public OidcUserInfo getUserInfo() {
        return null;
    }

    @Override
    public OidcIdToken getIdToken() {
        return null;
    }

}
