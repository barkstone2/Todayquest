package todayquest.user.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import todayquest.quest.entity.QuestDifficulty;
import todayquest.user.dto.UserPrincipal;
import todayquest.user.entity.ProviderType;
import todayquest.user.entity.UserInfo;
import todayquest.user.repository.UserRepository;

import javax.persistence.EntityManager;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.oauth2.core.OAuth2AccessToken.TokenType.BEARER;

@DisplayName("유저 서비스 유닛 테스트")
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks UserService userService;
    @Mock UserRepository userRepository;
    @Mock EntityManager em;
    @Mock ResourceLoader resourceLoader;

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
        when(userRepository.existsByNickname(duplicateName)).thenReturn(true);
        when(userRepository.existsByNickname(notDuplicateName)).thenReturn(false);

        //when
        boolean isDuplicate = userService.isDuplicateNickname(duplicateName);
        boolean isNotDuplicate = userService.isDuplicateNickname(notDuplicateName);

        //then
        verify(userRepository).existsByNickname(duplicateName);
        verify(userRepository).existsByNickname(notDuplicateName);
        assertThat(isDuplicate).isTrue();
        assertThat(isNotDuplicate).isFalse();
    }

    @DisplayName("신규 유저 등록 테스트")
    @Test
    public void testProcessUserInfoReg() throws Exception {
        //given
        Long userId = 1L;
        String oauth2Id = "test-id-1111";

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
                .id(userId)
                .providerType(ProviderType.GOOGLE)
                .nickname("nickname")
                .oauth2Id(oauth2Id)
                .build();

        OAuth2User oauth2User = new DefaultOAuth2User(
                null,
                Map.of("response", Map.of("id", oauth2Id),
                        "oauth2-id", oauth2Id),
                "oauth2-id");

        when(userRepository.findByOauth2Id(oauth2Id)).thenReturn(null);
        when(userRepository.saveAndFlush(any(UserInfo.class))).thenReturn(userInfo);
        when(userRepository.getById(userId)).thenReturn(userInfo);

        //when
        UserPrincipal savedUserPrincipal = userService.processUserInfo(request, oauth2User);

        //then
        verify(userRepository).findByOauth2Id(oauth2Id);
        verify(userRepository).existsByNickname(anyString());
        verify(userRepository).saveAndFlush(any(UserInfo.class));
        verify(em).detach(userInfo);
        verify(userRepository).getById(userInfo.getId());

        assertThat(savedUserPrincipal.getUserId())
                .isEqualTo(userInfo.getId());
    }

    @DisplayName("기존 유저 로그인 테스트")
    @Test
    public void testProcessUserInfoLogin() throws Exception {
        //given
        Long userId = 1L;
        String oauth2Id = "test-id-1111";

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
                .id(userId)
                .providerType(ProviderType.GOOGLE)
                .nickname("nickname")
                .oauth2Id(oauth2Id)
                .build();

        OAuth2User oauth2User = new DefaultOAuth2User(
                null,
                Map.of("response", Map.of("id", oauth2Id),
                        "oauth2-id", oauth2Id),
                "oauth2-id");

        when(userRepository.findByOauth2Id(oauth2Id)).thenReturn(userInfo);

        //when
        UserPrincipal savedUserPrincipal = userService.processUserInfo(request, oauth2User);

        //then
        verify(userRepository).findByOauth2Id(oauth2Id);
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(em);

        assertThat(savedUserPrincipal.getUserId())
                .isEqualTo(userInfo.getId());
    }

    @DisplayName("닉네임 변경 테스트")
    @Test
    public void testChangeNickname() throws Exception {
        //given
        Long userId = 1L;

        String oldNickname = "oldNickname";
        String newNickname = "newNickname";

        UserInfo user = UserInfo.builder()
                .id(userId).nickname(oldNickname).build();

        UserPrincipal principal = UserPrincipal.builder()
                .userId(userId).nickname(oldNickname).build();

        when(userRepository.getById(userId)).thenReturn(user);

        //when
        userService.changeNickname(principal, newNickname);

        //then
        verify(userRepository).getById(userId);
        assertThat(user.getNickname()).isEqualTo(newNickname);
        assertThat(principal.getNickname()).isEqualTo(newNickname);
    }


    @DisplayName("경험치, 골드 획득 테스트")
    @Test
    public void testEarnExpAndGold() throws IOException {
        //given
        Long userId = 1L;
        UserInfo user = UserInfo.builder()
                .id(userId).level(1).exp(0L).gold(0L).build();

        UserPrincipal principal = UserPrincipal.builder()
                .userId(userId).build();
        principal.setAttributes(new HashMap<>());
        principal.synchronizeUserInfo(user);

        QuestDifficulty clearInfo = QuestDifficulty.EASY;

        when(resourceLoader.getResource(anyString())).thenReturn(new ClassPathResource("data/exp_table.json"));

        //when
        userService.earnExpAndGold(user, clearInfo, principal);


        //then
        verify(resourceLoader).getResource(anyString());
        assertThat(user.getExp()).isEqualTo(clearInfo.getExperience());
        assertThat(user.getGold()).isEqualTo(clearInfo.getGold());
        assertThat(principal.getExp()).isEqualTo(clearInfo.getExperience());
        assertThat(principal.getGold()).isEqualTo(clearInfo.getGold());

    }

}