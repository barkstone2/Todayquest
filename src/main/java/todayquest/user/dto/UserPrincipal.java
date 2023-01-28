package todayquest.user.dto;

import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import todayquest.user.entity.ProviderType;
import todayquest.user.entity.UserInfo;

import java.time.LocalTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Getter @Setter
@RequiredArgsConstructor @Builder
@AllArgsConstructor
public class UserPrincipal implements OAuth2User, OidcUser, UserDetails {

    private final Long userId;
    private final ProviderType providerType;
    private final Collection<GrantedAuthority> authorities;

    private String nickname;
    private Map<String, Object> attributes;

    public static UserPrincipal create(UserInfo userInfo) {

        Map<String, Object> attr = new HashMap<>();
        attr.put("level", userInfo.getLevel());
        attr.put("gold", userInfo.getGold());
        attr.put("exp", userInfo.getExp());
        attr.put("resetTime", userInfo.getResetTime().getHour());
        attr.put("coreTime", userInfo.getCoreTime().getHour());

        return UserPrincipal.builder()
                .userId(userInfo.getId())
                .nickname(userInfo.getNickname())
                .providerType(userInfo.getProviderType())
                .attributes(attr)
                .authorities(Collections.singletonList(new SimpleGrantedAuthority(RoleType.USER.getCode())))
                .build();
    }


    public static UserPrincipal create(UserInfo userInfo, Map<String, Object> attributes) {
        UserPrincipal userPrincipal = create(userInfo);
        userPrincipal.setAttributes(attributes);

        return userPrincipal;
    }

    public Integer getLevel() {
        return (Integer) attributes.get("level");
    }

    public Long getExp() {
        return (Long) attributes.get("exp");
    }

    public Long getGold() {
        return (Long) attributes.get("gold");
    }

    public void synchronizeUserInfo(UserInfo user) {
        attributes.put("level", user.getLevel());
        attributes.put("exp", user.getExp());
        attributes.put("gold", user.getGold());
    }

    public void changeUserSettings(UserRequestDto dto) {
        attributes.put("resetTime", dto.getResetTime());
        attributes.put("coreTime", dto.getCoreTime());
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
