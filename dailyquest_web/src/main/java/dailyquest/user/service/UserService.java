package dailyquest.user.service;

import dailyquest.quest.entity.QuestType;
import dailyquest.redis.service.RedisService;
import dailyquest.user.dto.UserExpAndGoldRequest;
import dailyquest.user.dto.UserPrincipal;
import dailyquest.user.dto.UserSaveRequest;
import dailyquest.user.dto.UserUpdateRequest;
import dailyquest.user.entity.ProviderType;
import dailyquest.user.entity.UserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@RequiredArgsConstructor
@Service
public class UserService {
    private final UserQueryService userQueryService;
    private final UserCommandService userCommandService;
    private final RedisService redisService;

    public UserPrincipal getOrRegisterUser(String oauth2Id, ProviderType providerType) {
        UserInfo foundUser = userQueryService.findUser(oauth2Id);
        boolean needSave = foundUser == null;
        if (needSave) {
            String randomNickname = this.createRandomNickname();
            UserSaveRequest saveRequest = new UserSaveRequest(oauth2Id, randomNickname, providerType);
            foundUser = userCommandService.saveUser(saveRequest);
        }
        return UserPrincipal.create(foundUser, redisService.getExpTable());
    }

    private String createRandomNickname() {
        String candidateNickname = redisService.createRandomNickname();
        while (userQueryService.isDuplicateNickname(candidateNickname)) {
            candidateNickname = redisService.createRandomNickname();
        }
        return candidateNickname;
    }

    public UserPrincipal getUserById(Long userId) {
        UserInfo userInfo = userQueryService.findUser(userId);
        return UserPrincipal.create(userInfo, redisService.getExpTable());
    }

    public void changeUserSettings(UserPrincipal principal, UserUpdateRequest updateRequest) {
        UserInfo updateTarget = userQueryService.findUser(principal.getId());
        userCommandService.updateUser(updateTarget, updateRequest);
    }

    public void earnExpAndGold(QuestType type, UserInfo user) {
        UserInfo foundUser = userQueryService.findUser(user.getId());
        long questClearExp = redisService.getQuestClearExp();
        long questClearGold = redisService.getQuestClearGold();
        UserExpAndGoldRequest updateRequest = UserExpAndGoldRequest.of(type, questClearExp, questClearGold);
        userCommandService.updateUserExpAndGold(foundUser, updateRequest);
    }
}
