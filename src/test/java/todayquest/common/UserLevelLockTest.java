package todayquest.common;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import todayquest.quest.dto.QuestRequestDto;
import todayquest.quest.entity.Quest;
import todayquest.quest.entity.QuestDifficulty;
import todayquest.quest.repository.QuestRepository;
import todayquest.quest.service.QuestService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

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


    @DisplayName("네임드 락 멀티 스레드 테스트")
    @Test
    public void testNamedLock() throws Exception {
        //given
        QuestRequestDto dto = QuestRequestDto.builder()
                .title("test")
                .description("test")
                .difficulty(QuestDifficulty.easy)
                .isRepeat(true)
                .deadLineDate(LocalDate.now())
                .deadLineTime(LocalTime.now())
                .rewards(new ArrayList<>())
                .build();

        Long userId = 1L;

        AtomicLong result1 = new AtomicLong(1L);
        AtomicLong result2 = new AtomicLong(2L);

        ExecutorService executorService = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(2);

        executorService.execute(() -> {
            userLevelLock.executeWithLock(
                    "QUEST_SEQ" + userId,
                    3,
                    () -> questService.saveQuest(dto, userId)
            );
            latch.countDown();
        });

        executorService.execute(() -> {
            userLevelLock.executeWithLock(
                    "QUEST_SEQ" + userId,
                    3,
                    () -> questService.saveQuest(dto, userId)
            );
            latch.countDown();
        });

        latch.await();

        Quest savedQuest1 = questRepository.getById(result1.get());
        Quest savedQuest2 = questRepository.getById(result2.get());

        assertThat(savedQuest1.getSeq()).isNotEqualTo(savedQuest2.getSeq());
    }

}
