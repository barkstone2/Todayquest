package todayquest.user.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import todayquest.common.MessageUtil;
import todayquest.quest.entity.QuestType;
import todayquest.user.dto.UserPrincipal;
import todayquest.user.dto.UserRequestDto;
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

    public void earnExpAndGold(QuestType type, UserInfo user) throws IOException {
        // 경험치 테이블을 읽어온다.
        Resource resource = resourceLoader.getResource("classpath:data/exp_table.json");
        ObjectMapper om = new ObjectMapper();
        Map<Integer, Long> expTable = om.readValue(resource.getInputStream(), new TypeReference<>() {});
        Long targetExp = expTable.get(user.getLevel());

        user.earnExpAndGold(type, targetExp);
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
