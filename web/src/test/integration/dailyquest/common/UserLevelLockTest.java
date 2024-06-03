package dailyquest.common;

import dailyquest.context.IntegrationTestContext;
import dailyquest.context.MockElasticsearchTestContextConfig;
import dailyquest.context.MockRedisTestContextConfig;
import dailyquest.quest.dto.WebQuestRequest;
import dailyquest.quest.repository.QuestRepository;
import dailyquest.quest.service.QuestService;
import dailyquest.user.dto.UserSaveRequest;
import dailyquest.user.entity.ProviderType;
import dailyquest.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

@Import({MockRedisTestContextConfig.class, MockElasticsearchTestContextConfig.class})
@Slf4j
@DisplayName("유저 레벨 락 테스트")
public class UserLevelLockTest extends IntegrationTestContext {

    @Autowired
    UserLevelLock userLevelLock;

    @Autowired
    QuestService questService;

    @Autowired
    QuestRepository questRepository;

    @Autowired
    UserService userService;


    @DisplayName("네임드 락 멀티 스레드 테스트")
    @Test
    public void testNamedLock() throws Exception {
        //given
        WebQuestRequest dto = new WebQuestRequest("test", "test", Collections.emptyList(), null, null);

        int threadCount = 5;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        Future<Long> resultUserId = executorService.submit(() -> {
            UserSaveRequest userSaveRequest = new UserSaveRequest("user1", "user1", ProviderType.GOOGLE);
            long userId = userService.saveUser(userSaveRequest);

            //when
            for (int i = 0; i < threadCount; i++) {
                executorService.execute(() -> {
                    userLevelLock.executeWithLock(
                            "QUEST_SEQ" + userId,
                            3,
                            () -> questService.saveQuest(dto, userId)
                    );
                    latch.countDown();
                });
            }

            return userId;
        });

        Long beforeSeq = questRepository.getNextSeqOfUser(resultUserId.get());
        latch.await();

        //then
        Long afterSeq = questRepository.getNextSeqOfUser(resultUserId.get());
        assertThat(beforeSeq + threadCount).isEqualTo(afterSeq);
    }

}
