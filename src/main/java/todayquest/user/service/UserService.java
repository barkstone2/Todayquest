package todayquest.user.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import todayquest.common.MessageUtil;
import todayquest.quest.entity.QuestType;
import todayquest.user.dto.UserPrincipal;
import todayquest.user.dto.UserRequestDto;
import todayquest.user.entity.ProviderType;
import todayquest.user.entity.UserInfo;
import todayquest.user.repository.UserRepository;

import java.io.IOException;
import java.util.Map;
import java.util.Random;

@Transactional
@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final EntityManager em;
    private final ResourceLoader resourceLoader;

    public UserPrincipal processUserInfo(OidcUserRequest request, OidcUser user) {
        return processUserInfo((OAuth2UserRequest) request, user);
    }

    public UserPrincipal processUserInfo(OAuth2UserRequest request, OAuth2User user) {
        ProviderType providerType = ProviderType.valueOf(request.getClientRegistration().getRegistrationId().toUpperCase());

        // Oauth2 Provider 가 제공하는 ID 값 추출
        String id = user.getName();
        if(providerType.name().equals("NAVER")) {
            Map<String, Object> info = user.getAttribute("response");
            id = (String) info.get("id");
        }

        // 해당 Provider ID로 가입된 회원이 있나 조회
        UserInfo savedUserInfo = userRepository.findByOauth2Id(id);

        // 가입 이력이 없으면 유저 정보를 DB에 등록
        if(savedUserInfo == null) {
            String tempNickName = createRandomNickname();

            while (isDuplicateNickname(tempNickName)) {
                tempNickName = createRandomNickname();
            }

            UserInfo newUserInfo = new UserInfo(id, tempNickName, providerType);

            // Dynamic Insert로 처리한 컬럼의 default 값을 반환하지 않음
            UserInfo savedUser = userRepository.saveAndFlush(newUserInfo);

            // 영속성 컨텍스트에 저장한 엔티티를 준영속 상태로 만듬
            em.detach(savedUser);

            // 별도의 조회 쿼리로 엔티티 교체
            savedUserInfo = userRepository.getById(savedUser.getId());
        }

        // 유저 엔티티의 정보로 Principal을 만들어 반환
        return UserPrincipal.create(savedUserInfo);
    }

    public UserPrincipal getUserInfoById(Long id) {
        UserInfo userInfo = userRepository.getReferenceById(id);
        return UserPrincipal.create(userInfo);
    }

    public boolean isDuplicateNickname(String nickname) {
        return userRepository.existsByNickname(nickname);
    }

    public void changeUserSettings(UserPrincipal principal, UserRequestDto dto) {
        UserInfo findUser = userRepository.getById(principal.getUserId());

        String nickname = dto.getNickname();
        if(nickname != null) {
            String nicknameTrim = nickname.trim();
            boolean isDuplicated = isDuplicateNickname(nicknameTrim);
            if (isDuplicated) {
                throw new IllegalStateException(MessageUtil.getMessage("nickname.duplicate"));
            }

            findUser.updateNickname(nicknameTrim);
            principal.setNickname(nicknameTrim);
        }

        findUser.changeUserSettings(dto);
        principal.changeUserSettings(dto);
    }

    public void earnExpAndGold(QuestType type, UserInfo user, UserPrincipal principal) throws IOException {
        // 경험치 테이블을 읽어온다.
        Resource resource = resourceLoader.getResource("classpath:data/exp_table.json");
        ObjectMapper om = new ObjectMapper();
        Map<Integer, Long> expTable = om.readValue(resource.getInputStream(), new TypeReference<>() {});
        Long targetExp = expTable.get(user.getLevel());

        Long earnGold = 1L;
        Long earnExp = 0L;

        if(type.equals(QuestType.MAIN)) earnExp = 1L;

        // 사용자의 경험치와 골드를 증가시킨다.
        user.earnExpAndGold(earnExp, earnGold, targetExp);

        // 로그인된 세션의 정보를 동기화한다.
        principal.synchronizeUserInfo(user);
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
