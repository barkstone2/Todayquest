package todayquest.user.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import todayquest.common.MessageUtil;
import todayquest.exception.DuplicateNicknameException;
import todayquest.exception.RedisDataNotFoundException;
import todayquest.quest.entity.QuestType;
import todayquest.user.dto.UserPrincipal;
import todayquest.user.dto.UserRequestDto;
import todayquest.user.entity.UserInfo;
import todayquest.user.repository.UserRepository;

import java.util.Random;

@Transactional
@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final EntityManager em;
    private final RedisTemplate<String, String> redisTemplate;

    public UserPrincipal getUserInfoById(Long id) {
        UserInfo userInfo = userRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException(MessageUtil.getMessage("exception.badRequest")));
        return UserPrincipal.create(userInfo);
    }

    public boolean isDuplicateNickname(String nickname) {
        return userRepository.existsByNickname(nickname);
    }

    public void changeUserSettings(UserPrincipal principal, UserRequestDto dto) {
        UserInfo findUser = userRepository.getReferenceById(principal.getUserId());

        String nickname = dto.getNickname();
        if(nickname != null) {
            String nicknameTrim = nickname.trim();
            boolean isDuplicated = isDuplicateNickname(nicknameTrim);
            if (isDuplicated) {
                throw new DuplicateNicknameException(MessageUtil.getMessage("nickname.duplicate"));
            }

            findUser.updateNickname(nicknameTrim);
            principal.setNickname(nicknameTrim);
        }

        findUser.changeUserSettings(dto);
        principal.changeUserSettings(dto);
    }

    public void earnExpAndGold(QuestType type, UserInfo user) {
        Long targetExp = redisTemplate.<String, Long>opsForHash().get("exp_table", user.getLevel());
        if(targetExp == null) throw new RedisDataNotFoundException(MessageUtil.getMessage("exception.server.error"));
        user.earnExpAndGold(type, targetExp);
    }

    public String createRandomNickname() {
        String nicknamePrefix = redisTemplate.opsForSet().randomMember("nickname_prefix");
        String nicknamePostfix = redisTemplate.opsForSet().randomMember("nickname_postfix");

        return nicknamePrefix + nicknamePostfix +
                new Random().nextInt(1_000_000_000);
    }
}
