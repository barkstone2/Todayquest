package dailyquest.user.service;

import dailyquest.common.DateTimeExtensionKt;
import dailyquest.common.MessageUtil;
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

import java.time.LocalDateTime;

@Transactional
@RequiredArgsConstructor
@Service
public class UserService {
    private final UserQueryService userQueryService;
    private final UserCommandService userCommandService;
    private final RedisService redisService;

    public UserInfo getOrSaveUser(String oauth2Id, ProviderType providerType) {
        UserInfo foundUser = userQueryService.findUser(oauth2Id);
        boolean needSave = foundUser == null;
        if (needSave) {
            String randomNickname = this.createRandomNickname();
            UserSaveRequest saveRequest = new UserSaveRequest(oauth2Id, randomNickname, providerType);
            foundUser = userCommandService.saveUser(saveRequest);
        }
        return foundUser;
    }

    private String createRandomNickname() {
        String candidateNickname = redisService.createRandomNickname();
        while (userQueryService.isDuplicateNickname(candidateNickname)) {
            candidateNickname = redisService.createRandomNickname();
        }
        return candidateNickname;
    }

    public UserInfo getUser(Long userId) {
        return userQueryService.findUser(userId);
    }

    public UserPrincipal getUserPrincipal(Long userId) {
        return UserPrincipal.create(this.getUser(userId), redisService.getExpTable());
    }

    public void updateUser(UserPrincipal principal, UserUpdateRequest updateRequest) throws IllegalStateException {
        UserInfo updateTarget = userQueryService.findUser(principal.getId());
        boolean updateFailed = !userCommandService.updateUser(updateTarget, updateRequest);
        if (updateFailed) {
            LocalDateTime updateAvailableTime = updateTarget.getUpdateAvailableDateTimeOfCoreTime();
            String timeSinceNowUntilAvailable = DateTimeExtensionKt.timeSinceNowAsString(updateAvailableTime);
            String errorMessage = MessageUtil.getMessage("user.settings.updateLimit", timeSinceNowUntilAvailable);
            throw new IllegalStateException(errorMessage);
        }
    }

    public void giveExpAndGoldToUser(QuestType type, UserInfo user) {
        UserInfo foundUser = userQueryService.findUser(user.getId());
        long questClearExp = redisService.getQuestClearExp();
        long questClearGold = redisService.getQuestClearGold();
        UserExpAndGoldRequest updateRequest = UserExpAndGoldRequest.of(type, questClearExp, questClearGold);
        userCommandService.updateUserExpAndGold(foundUser, updateRequest);
    }
}
