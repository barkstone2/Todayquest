package dailyquest.user.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import dailyquest.common.MessageUtil;
import dailyquest.exception.RedisDataNotFoundException;
import dailyquest.properties.RedisKeyProperties;
import dailyquest.quest.entity.QuestType;
import dailyquest.user.dto.UserPrincipal;
import dailyquest.user.dto.UserRequestDto;
import dailyquest.user.entity.ProviderType;
import dailyquest.user.entity.UserInfo;
import dailyquest.user.repository.UserRepository;

import java.time.Duration;
import java.time.LocalDateTime;
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

        Map<Integer, Long> expTable = redisTemplate.<Integer, Long>opsForHash().entries(redisKeyProperties.getExpTable());

        return UserPrincipal.create(savedUserInfo, expTable);
    }

    public UserPrincipal getUserById(Long id) {
        UserInfo userInfo = userRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException(MessageUtil.getMessage("exception.badRequest")));

        Map<Integer, Long> expTable = redisTemplate.<Integer, Long>opsForHash().entries(redisKeyProperties.getExpTable());

        return UserPrincipal.create(userInfo, expTable);
    }

    public boolean isDuplicateNickname(String nickname) {
        return userRepository.existsByNickname(nickname);
    }

    public void changeUserSettings(UserPrincipal principal, UserRequestDto dto) {
        UserInfo findUser = userRepository.getReferenceById(principal.getId());
        LocalDateTime requestedDate = LocalDateTime.now().withSecond(0).withNano(0);

        findUser.updateNickname(dto.getNickname());

        if(!findUser.updateCoreTime(dto.getCoreTime(), requestedDate)) {
            Duration diff = findUser.getRemainTimeUntilCoreTimeUpdateAvailable(requestedDate);
            String diffStr = String.format("%d시간 %d분", diff.toHours(), diff.toMinutes() % 60);
            throw new IllegalStateException(MessageUtil.getMessage("user.settings.update_limit", MessageUtil.getMessage("user.settings.core_time"), diffStr));
        }

    }

    public void earnExpAndGold(QuestType type, UserInfo user) {
        BoundHashOperations<String, String, Long> ops = redisTemplate.boundHashOps(redisKeyProperties.getSettings());

        Long questClearExp = ops.get(redisKeyProperties.getQuestClearExp());
        Long questClearGold = ops.get(redisKeyProperties.getQuestClearGold());

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
