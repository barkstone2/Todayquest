package todayquest.common;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import todayquest.quest.dto.QuestRequest;
import todayquest.quest.repository.QuestRepository;
import todayquest.quest.service.QuestService;
import todayquest.user.entity.ProviderType;
import todayquest.user.service.UserService;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@Slf4j
@DisplayName("유저 레벨 락 테스트")
@Transactional
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class UserLevelLockTest {

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
        QuestRequest dto = new QuestRequest("test", "test", null, null);

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
