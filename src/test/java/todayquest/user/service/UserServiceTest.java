package todayquest.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import todayquest.user.dto.UserPrincipal;
import todayquest.user.entity.ProviderType;
import todayquest.user.entity.UserInfo;
import todayquest.user.repository.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.oauth2.core.OAuth2AccessToken.TokenType.BEARER;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserRepository userRepository;

    @DisplayName("랜덤 닉네임 생성 메서드 테스트")
    @Test
    public void testCreateRandomNickname() throws Exception {
        String randomNickname = userService.createRandomNickname();
        assertThat(randomNickname).isNotEmpty();
    }

    @DisplayName("닉네임 중복 확인 메서드 테스트")
    @Test
    public void testIsDuplicate() throws Exception {
        //given
        String duplicateName = "duplicate";
        String notDuplicateName = "notDuplicate";
        given(userService.isDuplicateNickname(duplicateName)).willReturn(true);
        given(userService.isDuplicateNickname(notDuplicateName)).willReturn(false);

        //when
        boolean isDuplicate = userService.isDuplicateNickname(duplicateName);
        boolean isNotDuplicate = userService.isDuplicateNickname(notDuplicateName);

        //then
        assertThat(isDuplicate).isTrue();
        assertThat(isNotDuplicate).isFalse();
    }

    @DisplayName("신규 유저 등록 테스트")
    @Test
    public void testProcessUserInfo() throws Exception {
        //given
        OAuth2UserRequest request = new OAuth2UserRequest(
                ClientRegistration.withRegistrationId("GOOGLE")
                        .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                        .clientId("client-id")
                        .redirectUri("uri")
                        .authorizationUri("uri")
                        .clientName("name")
                        .tokenUri("uri")
                        .build(),
                new OAuth2AccessToken(BEARER, "tokenValue", null, null)
        );
        UserInfo userInfo = UserInfo.builder()
                .id(1L)
                .providerType(ProviderType.GOOGLE)
                .nickname("test")
                .oauth2Id("test-id-1111")
                .build();
        UserPrincipal userPrincipal = UserPrincipal.create(userInfo);

        given(userRepository.findByOauth2Id(any())).willReturn(userInfo);

        //when
        UserPrincipal savedUser = userService.processUserInfo(request, userPrincipal);

        //then
        assertThat(savedUser.getUserId()).isEqualTo(userPrincipal.getUserId());
    }

}