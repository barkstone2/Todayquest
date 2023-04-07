package todayquest.user.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import todayquest.common.MessageUtil;
import todayquest.exception.DuplicateNicknameException;
import todayquest.exception.RedisDataNotFoundException;
import todayquest.properties.RedisKeyProperties;
import todayquest.quest.entity.QuestType;
import todayquest.user.dto.UserPrincipal;
import todayquest.user.dto.UserRequestDto;
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
    private final EntityManager em;
    private final RedisTemplate<String, String> redisTemplate;
    private final RedisKeyProperties redisKeyProperties;

    public UserPrincipal getOrRegisterUser(String oauth2Id, ProviderType providerType) {
        UserInfo savedUserInfo = userRepository.findByOauth2Id(oauth2Id);

        // 가입 이력이 없으면 유저 정보를 DB에 등록
        if (savedUserInfo == null) {
            String tempNickName = createRandomNickname();
            while (isDuplicateNickname(tempNickName)) {
                tempNickName = createRandomNickname();
            }
            UserInfo newUserInfo = new UserInfo(oauth2Id, tempNickName, providerType);

            // Dynamic Insert로 처리한 컬럼의 default 값을 반환하지 않음
            UserInfo savedUser = userRepository.saveAndFlush(newUserInfo);
            em.detach(savedUser);

            savedUserInfo = userRepository.getReferenceById(savedUser.getId());
        }

        Map<String, Long> expTable = redisTemplate.<String, Long>opsForHash().entries(redisKeyProperties.getExpTable());

        return UserPrincipal.create(savedUserInfo, expTable);
    }

    public UserPrincipal getUserById(Long id) {
        UserInfo userInfo = userRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException(MessageUtil.getMessage("exception.badRequest")));

        Map<String, Long> expTable = redisTemplate.<String, Long>opsForHash().entries(redisKeyProperties.getExpTable());

        return UserPrincipal.create(userInfo, expTable);
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
        }

        findUser.changeUserSettings(dto);
    }

    public void earnExpAndGold(QuestType type, UserInfo user) {
        Map<String, Integer> settings = redisTemplate.<String, Integer>opsForHash().entries(redisKeyProperties.getSettings());

        Integer questClearExp = settings.get(redisKeyProperties.getQuestClearExp());
        Integer questClearGold = settings.get(redisKeyProperties.getQuestClearGold());

        if (questClearExp == null || questClearGold == null) {
            throw new RedisDataNotFoundException(MessageUtil.getMessage("exception.server.error"));
        }

        user.updateExpAndGold(type, questClearExp, questClearGold);
    }

    private String createRandomNickname() {
        String nicknamePrefix = redisTemplate.opsForSet().randomMember(redisKeyProperties.getNicknamePrefix());
        String nicknamePostfix = redisTemplate.opsForSet().randomMember(redisKeyProperties.getNicknamePostfix());

        return nicknamePrefix + nicknamePostfix +
                new Random().nextInt(1_000_000_000);
    }
}
