package dailyquest.user.service;

import dailyquest.user.dto.UserExpAndGoldRequest;
import dailyquest.user.dto.UserSaveRequest;
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
import dailyquest.user.dto.UserUpdateRequest;
import dailyquest.user.entity.ProviderType;
import dailyquest.user.entity.UserInfo;

import java.util.Map;
import java.util.Random;

@Transactional
@RequiredArgsConstructor
@Service
public class UserService {
    private final UserQueryService userQueryService;
    private final UserCommandService userCommandService;
    private final RedisTemplate<String, String> redisTemplate;
    private final RedisKeyProperties redisKeyProperties;

    public UserPrincipal getOrRegisterUser(String oauth2Id, ProviderType providerType) {
        UserInfo foundUser = userQueryService.findUser(oauth2Id);
        boolean needSave = foundUser == null;
        if (needSave) {
            String randomNickname = this.createRandomNickname();
            while (userQueryService.isDuplicateNickname(randomNickname)) {
                randomNickname = this.createRandomNickname();
            }
            UserSaveRequest saveRequest = new UserSaveRequest(oauth2Id, randomNickname, providerType);
            foundUser = userCommandService.saveUser(saveRequest);
        }
        Map<Integer, Long> expTable = redisTemplate.<Integer, Long>opsForHash().entries(redisKeyProperties.getExpTable());
        return UserPrincipal.create(foundUser, expTable);
    }

    private String createRandomNickname() {
        String nicknamePrefix = redisTemplate.opsForSet().randomMember(redisKeyProperties.getNicknamePrefix());
        String nicknamePostfix = redisTemplate.opsForSet().randomMember(redisKeyProperties.getNicknamePostfix());
        return nicknamePrefix + nicknamePostfix + new Random().nextInt(1_000_000_000);
    }

    public UserPrincipal getUserById(Long userId) {
        UserInfo userInfo = userQueryService.findUser(userId);
        Map<Integer, Long> expTable = redisTemplate.<Integer, Long>opsForHash().entries(redisKeyProperties.getExpTable());
        return UserPrincipal.create(userInfo, expTable);
    }

    public void changeUserSettings(UserPrincipal principal, UserUpdateRequest updateRequest) {
        UserInfo updateTarget = userQueryService.findUser(principal.getId());
        userCommandService.updateUser(updateTarget, updateRequest);
    }

    public void earnExpAndGold(QuestType type, UserInfo user) {
        BoundHashOperations<String, String, Long> ops = redisTemplate.boundHashOps(redisKeyProperties.getSettings());
        Long questClearExp = ops.get(redisKeyProperties.getQuestClearExp());
        Long questClearGold = ops.get(redisKeyProperties.getQuestClearGold());
        if (questClearExp == null || questClearGold == null) {
            throw new RedisDataNotFoundException(MessageUtil.getMessage("exception.server.error"));
        }
        UserExpAndGoldRequest updateRequest = UserExpAndGoldRequest.of(type, questClearExp, questClearGold);
        userCommandService.updateUserExpAndGold(user, updateRequest);
    }
}
