package dailyquest.quest.service;

import dailyquest.common.MessageUtil;
import dailyquest.quest.dto.QuestResponse;
import dailyquest.quest.entity.Quest;
import dailyquest.quest.entity.QuestState;
import dailyquest.quest.repository.QuestRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("퀘스트 쿼리 서비스 유닛 테스트")
@ExtendWith(MockitoExtension.class)
public class QuestQueryServiceUnitTest {

    @InjectMocks
    QuestQueryService questQueryService;
    @Mock
    QuestRepository questRepository;
    MockedStatic<MessageUtil> messageUtil;

    @BeforeEach
    void beforeEach() {
        messageUtil = mockStatic(MessageUtil.class);
        when(MessageUtil.getMessage(any())).thenReturn("");
        when(MessageUtil.getMessage(any(), any())).thenReturn("");
    }

    @AfterEach
    void afterEach() {
        messageUtil.close();
    }

    @DisplayName("현재 퀘스트 조회 시")
    @Nested
    class CurrentQuestTest {

        @DisplayName("요청 파라미터가 제대로 전달된다")
        @Test
        void parametersDeliveredProperly() {
            //given
            Long userId = 1L;
            QuestState state = QuestState.PROCEED;
            List<Quest> list = List.of();

            doReturn(list).when(questRepository).getCurrentQuests(eq(userId), eq(state));

            // when
            questQueryService.getCurrentQuests(userId, state);

            // then
            verify(questRepository, times(1)).getCurrentQuests(eq(userId), eq(state));
        }
    }

    @DisplayName("퀘스트 조회 시")
    @Nested
    class GetQuestTest {

        @DisplayName("요청 유저의 퀘스트가 아니면 예외가 발생한다")
        @Test
        void ifNotQuestOfUserThanThrow() {
            //given
            Long questId = 0L;
            Long userId = 1L;
            Quest mockQuest = mock(Quest.class);

            doReturn(Optional.of(mockQuest)).when(questRepository).findById(eq(questId));
            doReturn(false).when(mockQuest).isQuestOfUser(eq(userId));

            //when
            Runnable run = () -> questQueryService.getQuestInfo(questId, userId);

            //then
            assertThatThrownBy(run::run).isInstanceOf(AccessDeniedException.class);
            verify(mockQuest, times(1)).isQuestOfUser(eq(userId));
        }

        @DisplayName("요청 유저의 퀘스트라면 예외가 발생하지 않는다")
        @Test
        void ifQuestOfUserThanDoesNotThrow() {
            //given
            Long questId = 0L;
            Long userId = 1L;
            Quest mockQuest = mock(Quest.class);

            doReturn(Optional.of(mockQuest)).when(questRepository).findById(eq(questId));
            doReturn(true).when(mockQuest).isQuestOfUser(eq(userId));

            //when
            questQueryService.getQuestInfo(questId, userId);

            //then
            verify(mockQuest, times(1)).isQuestOfUser(eq(userId));
        }
    }

    @DisplayName("findByIdOrThrow 메서드 호출 시")
    @Nested
    class TestFindByIdOrThrow {
        @DisplayName("엔티티 조회에 실패하면 EntityNotFound 예외를 던진다")
        @Test
        public void ifFailGetEntityByIdThenThrow() throws Exception {
            //given
            Long questId = 1L;
            doReturn(Optional.empty()).when(questRepository).findById(eq(questId));

            //when
            Runnable call = () -> questQueryService.findByIdOrThrow(questId);

            //then
            assertThatThrownBy(call::run).isInstanceOf(EntityNotFoundException.class);
        }

        @DisplayName("엔티티 조회 성공 시 엔티티를 반환한다")
        @Test
        public void ifSucceedThenReturn() throws Exception {
            //given
            Long questId = 1L;
            Quest mockQuest = mock(Quest.class);
            doReturn(Optional.of(mockQuest)).when(questRepository).findById(eq(questId));

            //when
            Quest result = questQueryService.findByIdOrThrow(questId);

            //then
            assertThat(result).isEqualTo(mockQuest);
        }
    }

}
