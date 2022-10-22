package todayquest.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import todayquest.user.dto.UserPrincipal;
import todayquest.user.entity.ProviderType;
import todayquest.user.entity.UserInfo;
import todayquest.user.repository.UserRepository;

import java.util.Map;
import java.util.Random;

@Transactional
@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;

    public UserPrincipal processUserInfo(OidcUserRequest request, OidcUser user) {
        return processUserInfo((OAuth2UserRequest) request, user);
    }

    public UserPrincipal processUserInfo(OAuth2UserRequest request, OAuth2User user) {
        ProviderType providerType = ProviderType.valueOf(request.getClientRegistration().getRegistrationId().toUpperCase());

        String id = user.getName();

        if(providerType.name().equals("NAVER")) {
            Map<String, Object> info = user.getAttribute("response");
            id = (String) info.get("id");
        }

        UserInfo savedUserInfo = userRepository.findByOauth2Id(id);

        if(savedUserInfo == null) {
            String tempNickName = createRandomNickname();

            while (isDuplicateNickname(tempNickName)) {
                tempNickName = createRandomNickname();
            }

            UserInfo newUserInfo = UserInfo.builder()
                    .oauth2Id(id)
                    .nickname(tempNickName)
                    .providerType(providerType)
                    .build();

            savedUserInfo = userRepository.saveAndFlush(newUserInfo);
        }
        return UserPrincipal.create(savedUserInfo);
    }

    public boolean isDuplicateNickname(String nickname) {
        return userRepository.existsByNickname(nickname);
    }

    public void changeNickname(UserPrincipal principal, String nickname) {
        UserInfo findUser = userRepository.getById(principal.getUserId());
        findUser.updateNickname(nickname);
        principal.setNickname(nickname);
    }
    public String createRandomNickname() {
        String[] nickNamePrefixPool = {"행복한", "즐거운", "아름다운", "기쁜", "빨간", "까만", "노란", "파란", "슬픈"};
        String[] nickNamePostfixPool = {"바지", "자동차", "비행기", "로봇", "강아지", "고양이", "트럭", "장갑", "신발", "토끼"};

        String tempNickName =
                nickNamePrefixPool[new Random().nextInt(nickNamePrefixPool.length)] +
                nickNamePostfixPool[new Random().nextInt(nickNamePostfixPool.length)]
                + new Random().nextInt(1_000_000_000);

        return tempNickName;
    }
}
