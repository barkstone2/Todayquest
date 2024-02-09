package dailyquest.common;

import dailyquest.context.IntegrationTestContextBaseConfig;
import dailyquest.context.MockElasticsearchTestContextConfig;
import dailyquest.context.MockRedisTestContextConfig;
import dailyquest.preferencequest.entity.PreferenceQuest;
import dailyquest.quest.dto.QuestRequest;
import dailyquest.quest.entity.Quest;
import dailyquest.quest.repository.QuestRepository;
import dailyquest.quest.service.QuestService;
import dailyquest.user.entity.ProviderType;
import dailyquest.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static dailyquest.common.UserLevelLockTest.UserLevelLockTestConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@Slf4j
@DisplayName("유저 레벨 락 테스트")
@Transactional
@SpringBootTest(
        webEnvironment = RANDOM_PORT,
        classes = UserLevelLockTestConfig.class
)
public class UserLevelLockTest {

    @Import({
            IntegrationTestContextBaseConfig.class,
            MockRedisTestContextConfig.class,
            MockElasticsearchTestContextConfig.class,
            UserLevelLock.class
    })
    @ComponentScan(basePackages = {"dailyquest.quest"})
    @EnableJpaRepositories(basePackageClasses = {QuestRepository.class})
    @EntityScan(basePackageClasses = {Quest.class, PreferenceQuest.class})
    static class UserLevelLockTestConfig { }

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
        QuestRequest dto = new QuestRequest("test", "test", Collections.emptyList(), null, null);

        int threadCount = 5;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        Future<Long> resultUserId = executorService.submit(() -> {
            Long userId = userService.getOrRegisterUser("user1", ProviderType.GOOGLE).getId();

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

        Long beforeSeq = questRepository.getNextSeqByUserId(resultUserId.get());
        latch.await();

        //then
        Long afterSeq = questRepository.getNextSeqByUserId(resultUserId.get());
        assertThat(beforeSeq + threadCount).isEqualTo(afterSeq);
    }

}
