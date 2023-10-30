package dailyquest.quest.service;

import dailyquest.common.MessageUtil;
import dailyquest.quest.dto.DetailInteractRequest;
import dailyquest.quest.dto.QuestRequest;
import dailyquest.quest.dto.QuestResponse;
import dailyquest.quest.entity.Quest;
import dailyquest.quest.entity.QuestType;
import dailyquest.quest.repository.QuestRepository;
import dailyquest.user.entity.UserInfo;
import dailyquest.user.repository.UserRepository;
import dailyquest.user.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("퀘스트 커맨드 서비스 유닛 테스트")
@ExtendWith(MockitoExtension.class)
public class QuestCommandServiceUnitTest {

    @InjectMocks QuestCommandService questCommandService;
    @Mock QuestQueryService questQueryService;
    @Mock QuestRepository questRepository;
    @Mock UserRepository userRepository;
    @Mock UserService userService;
    @Mock QuestLogService questLogService;
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


    @DisplayName("퀘스트 저장 시")
    @Nested
    class QuestSaveTest {

        @DisplayName("현재 시간이 유저의 코어타임이라면 타입 변경 로직을 호출한다")
        @Test
        void ifCoreTimeChangeToMainType() {
            //given
            QuestRequest mockDto = mock(QuestRequest.class);
            UserInfo mockUser = mock(UserInfo.class);
            Quest mockQuest = mock(Quest.class);
            Long userId = 0L;
            Long nextSeq = 1L;

            doReturn(true).when(mockUser).isNowCoreTime();
            doReturn(mockUser).when(userRepository).getReferenceById(eq(userId));
            doReturn(nextSeq).when(questRepository).getNextSeqByUserId(eq(userId));
            doReturn(mockQuest).when(mockDto).mapToEntity(eq(nextSeq), eq(mockUser));

            //when
            QuestResponse saveQuest = questCommandService.saveQuest(mockDto, userId);

            //then
            verify(mockDto, times(1)).checkRangeOfDeadLine(any());
            verify(mockUser, times(1)).isNowCoreTime();
            verify(mockDto, times(1)).toMainQuest();
            verify(questRepository, times(1)).getNextSeqByUserId(eq(userId));
            verify(questRepository, times(1)).save(mockQuest);
            verify(mockQuest, times(1)).updateDetailQuests(any());
            verify(questLogService, times(1)).saveQuestLog(mockQuest);
            assertThat(saveQuest).isInstanceOf(QuestResponse.class);
        }


        @DisplayName("현재 시간이 유저의 코어타임 아니라면 타입 변경 로직을 호출하지 않는다")
        @Test
        void ifNotCoreTimeDoesNotChangeType() {
            //given
            QuestRequest mockDto = mock(QuestRequest.class);
            UserInfo mockUser = mock(UserInfo.class);
            Quest mockQuest = mock(Quest.class);
            Long userId = 0L;
            Long nextSeq = 1L;

            doReturn(false).when(mockUser).isNowCoreTime();
            doReturn(mockUser).when(userRepository).getReferenceById(eq(userId));
            doReturn(nextSeq).when(questRepository).getNextSeqByUserId(eq(userId));
            doReturn(mockQuest).when(mockDto).mapToEntity(eq(nextSeq), eq(mockUser));

            //when
            QuestResponse saveQuest = questCommandService.saveQuest(mockDto, userId);

            //then
            verify(mockDto, times(1)).checkRangeOfDeadLine(any());
            verify(mockUser, times(1)).isNowCoreTime();
            verify(mockDto, times(0)).toMainQuest();
            verify(questRepository, times(1)).getNextSeqByUserId(eq(userId));
            verify(questRepository, times(1)).save(mockQuest);
            verify(mockQuest, times(1)).updateDetailQuests(any());
            verify(questLogService, times(1)).saveQuestLog(mockQuest);
            assertThat(saveQuest).isInstanceOf(QuestResponse.class);
        }

    }

    @DisplayName("퀘스트 수정 시")
    @Nested
    class QuestUpdateTest {

        @DisplayName("쿼리 서비스를 통해 엔티티를 조회한다")
        @Test
        void getEntityViaQueryService() {
            //given
            QuestRequest mockDto = mock(QuestRequest.class);
            Long questId = 0L;
            Long userId = 0L;
            doThrow(EntityNotFoundException.class).when(questQueryService).findByIdOrThrow(eq(questId));

            //when
            Runnable call = () -> questCommandService.updateQuest(mockDto, questId, userId);

            //then
            assertThatThrownBy(call::run).isInstanceOf(EntityNotFoundException.class);
            verify(questQueryService).findByIdOrThrow(eq(questId));
            verify(mockDto, times(0)).checkRangeOfDeadLine(any());
        }

        @DisplayName("데드라인 범위 체크 로직이 호출된다")
        @Test
        void invokeDeadLineCheckMethod() {
            //given
            QuestRequest mockDto = mock(QuestRequest.class);
            Quest mockQuest = mock(Quest.class);
            UserInfo mockUser = mock(UserInfo.class);
            Long questId = 0L;
            Long userId = 1L;

            doReturn(mockQuest).when(questQueryService).findByIdOrThrow(eq(questId));
            doReturn(mockUser).when(mockQuest).getUser();

            //when
            questCommandService.updateQuest(mockDto, questId, userId);

            //then
            verify(mockDto, times(1)).checkRangeOfDeadLine(any());
        }


        @DisplayName("퀘스트 소유주 검증 로직이 호출된다")
        @Test
        void invokeQuestOwnerCheckMethod() {
            //given
            QuestRequest mockDto = mock(QuestRequest.class);
            Quest mockQuest = mock(Quest.class);
            UserInfo mockUser = mock(UserInfo.class);
            Long questId = 0L;
            Long userId = 1L;

            doReturn(mockQuest).when(questQueryService).findByIdOrThrow(eq(questId));
            doReturn(mockUser).when(mockQuest).getUser();

            //when
            questCommandService.updateQuest(mockDto, questId, userId);

            //then
            verify(mockQuest, times(1)).checkOwnershipOrThrow(eq(userId));
        }


        @DisplayName("퀘스트 진행 상태 검증 로직이 호출된다")
        @Test
        void invokeProceedCheckMethod() {
            //given
            QuestRequest mockDto = mock(QuestRequest.class);
            Quest mockQuest = mock(Quest.class);
            UserInfo mockUser = mock(UserInfo.class);
            Long questId = 0L;
            Long userId = 1L;

            doReturn(mockQuest).when(questQueryService).findByIdOrThrow(eq(questId));
            doReturn(mockUser).when(mockQuest).getUser();

            //when
            questCommandService.updateQuest(mockDto, questId, userId);

            //then
            verify(mockQuest, times(1)).checkStateIsProceedOrThrow();
        }

        @DisplayName("기존 퀘스트가 메인 퀘스트라면 타입 변경 로직이 호출된다")
        @Test
        void ifMainQuestChangeTypeOfDto() {
            //given
            QuestRequest mockDto = mock(QuestRequest.class);
            Quest mockQuest = mock(Quest.class);
            UserInfo mockUser = mock(UserInfo.class);
            Long questId = 0L;
            Long userId = 0L;

            doReturn(mockUser).when(mockQuest).getUser();
            doReturn(mockQuest).when(questQueryService).findByIdOrThrow(eq(questId));

            doReturn(true).when(mockQuest).isMainQuest();

            //when
            questCommandService.updateQuest(mockDto, questId, userId);

            //then
            verify(mockQuest, times(1)).isMainQuest();
            verify(mockDto, times(1)).toMainQuest();
        }

        @DisplayName("기존 퀘스트가 서브 퀘스트라면 타입 변경 로직이 호출되지 않는다")
        @Test
        void ifNotMainQuestDoesNotChangeTypeOfDto() {
            //given
            QuestRequest mockDto = mock(QuestRequest.class);
            Quest mockQuest = mock(Quest.class);
            UserInfo mockUser = mock(UserInfo.class);
            Long questId = 0L;
            Long userId = 0L;

            doReturn(mockUser).when(mockQuest).getUser();
            doReturn(mockQuest).when(questQueryService).findByIdOrThrow(eq(questId));

            doReturn(false).when(mockQuest).isMainQuest();

            //when
            questCommandService.updateQuest(mockDto, questId, userId);

            //then
            verify(mockQuest, times(1)).isMainQuest();
            verify(mockDto, times(0)).toMainQuest();
        }

        @DisplayName("정상 호출일 경우 퀘스트 업데이트 로직이 호출된다")
        @Test
        void invokeQuestUpdateMethod() {
            //given
            QuestRequest mockDto = mock(QuestRequest.class);
            Quest mockQuest = mock(Quest.class);
            UserInfo mockUser = mock(UserInfo.class);
            Long questId = 0L;
            Long userId = 0L;

            doReturn(mockUser).when(mockQuest).getUser();
            doReturn(mockQuest).when(questQueryService).findByIdOrThrow(eq(questId));

            //when
            questCommandService.updateQuest(mockDto, questId, userId);

            //then
            verify(mockDto, times(1)).checkRangeOfDeadLine(any());
            verify(mockQuest, times(1)).checkOwnershipOrThrow(eq(userId));
            verify(mockQuest, times(1)).checkStateIsProceedOrThrow();
            verify(mockQuest, times(1)).isMainQuest();
            verify(mockQuest, times(1)).updateQuestEntity(mockDto);
        }
    }

    @DisplayName("퀘스트 삭제 시")
    @Nested
    class QuestDeleteTest {
        @DisplayName("쿼리 서비스를 통해 엔티티를 조회한다")
        @Test
        void getEntityViaQueryService() {
            //given
            Long questId = 0L;
            Long userId = 0L;
            doThrow(EntityNotFoundException.class).when(questQueryService).findByIdOrThrow(eq(questId));

            //when
            Runnable call = () -> questCommandService.deleteQuest(questId, userId);

            //then
            assertThatThrownBy(call::run).isInstanceOf(EntityNotFoundException.class);
        }

        @DisplayName("퀘스트 소유주 검증 로직이 호출된다")
        @Test
        void invokeQuestOwnerCheckMethod() {
            //given
            Long questId = 0L;
            Long userId = 1L;
            Quest mockQuest = mock(Quest.class);

            doReturn(mockQuest).when(questQueryService).findByIdOrThrow(eq(questId));

            //when
            questCommandService.deleteQuest(questId, userId);

            //then
            verify(mockQuest, times(1)).checkOwnershipOrThrow(eq(userId));
        }

        @DisplayName("정상 호출일 경우 퀘스트 삭제 로직이 호출된다")
        @Test
        void invokeQuestDeleteMethod() {
            //given
            Long questId = 0L;
            Long userId = 1L;
            Quest mockQuest = mock(Quest.class);

            doReturn(mockQuest).when(questQueryService).findByIdOrThrow(eq(questId));

            //when
            questCommandService.deleteQuest(questId, userId);

            //then
            verify(mockQuest, times(1)).deleteQuest();
        }
    }

    @DisplayName("퀘스트 완료 시")
    @Nested
    class QuestCompleteTest {
        @DisplayName("쿼리 서비스를 통해 엔티티를 조회한다")
        @Test
        void getEntityViaQueryService() {
            //given
            Long questId = 0L;
            Long userId = 0L;
            doThrow(EntityNotFoundException.class).when(questQueryService).findByIdOrThrow(eq(questId));

            //when
            Runnable call = () -> questCommandService.completeQuest(questId, userId);

            //then
            assertThatThrownBy(call::run).isInstanceOf(EntityNotFoundException.class);
        }

        @DisplayName("퀘스트 소유주 검증 로직이 호출된다")
        @Test
        void invokeQuestOwnerCheckMethod() {
            //given
            Long questId = 0L;
            Long userId = 1L;
            Quest mockQuest = mock(Quest.class);

            doReturn(mockQuest).when(questQueryService).findByIdOrThrow(eq(questId));

            //when
            questCommandService.completeQuest(questId, userId);

            //then
            verify(mockQuest, times(1)).checkOwnershipOrThrow(eq(userId));
        }

        @DisplayName("정상 호출일 경우 퀘스트 완료 로직이 호출된다")
        @Test
        void invokeQuestCompleteMethod() {
            //given
            Long questId = 0L;
            Long userId = 0L;
            Quest mockQuest = mock(Quest.class);
            UserInfo mockUser = mock(UserInfo.class);
            QuestType mockType = mock(QuestType.class);

            doReturn(mockQuest).when(questQueryService).findByIdOrThrow(eq(questId));
            doReturn(mockUser).when(mockQuest).getUser();
            doReturn(mockType).when(mockQuest).getType();

            //when
            questCommandService.completeQuest(questId, userId);

            //then
            verify(mockQuest, times(1)).completeQuest();
            verify(userService, times(1)).earnExpAndGold(eq(mockType), eq(mockUser));
            verify(questLogService, times(1)).saveQuestLog(eq(mockQuest));
        }
    }


    @DisplayName("퀘스트 포기 시")
    @Nested
    class QuestDiscardTest {

        @DisplayName("쿼리 서비스를 통해 엔티티를 조회한다")
        @Test
        void getEntityViaQueryService() {
            //given
            Long questId = 0L;
            Long userId = 0L;
            doThrow(EntityNotFoundException.class).when(questQueryService).findByIdOrThrow(eq(questId));

            //when
            Runnable call = () -> questCommandService.discardQuest(questId, userId);

            //then
            assertThatThrownBy(call::run).isInstanceOf(EntityNotFoundException.class);
        }

        @DisplayName("퀘스트 소유주 검증 로직이 호출된다")
        @Test
        void invokeQuestOwnerCheckMethod() {
            //given
            Long questId = 0L;
            Long userId = 1L;
            Quest mockQuest = mock(Quest.class);

            doReturn(mockQuest).when(questQueryService).findByIdOrThrow(eq(questId));

            //when
            questCommandService.discardQuest(questId, userId);

            //then
            verify(mockQuest, times(1)).checkOwnershipOrThrow(eq(userId));
        }

        @DisplayName("정상 호출일 경우 퀘스트 포기 로직이 호출된다")
        @Test
        void doDiscardQuest() {
            //given
            Long questId = 0L;
            Long userId = 1L;
            Quest mockQuest = mock(Quest.class);

            doReturn(mockQuest).when(questQueryService).findByIdOrThrow(eq(questId));

            //when
            questCommandService.discardQuest(questId, userId);

            //then
            verify(mockQuest, times(1)).discardQuest();
            verify(questLogService, times(1)).saveQuestLog(eq(mockQuest));
        }
    }

    @DisplayName("세부 퀘스트 상호 작용 시")
    @Nested
    class DetailQuestInteractTest {

        @DisplayName("쿼리 서비스를 통해 엔티티를 조회한다")
        @Test
        void getEntityViaQueryService() {
            //given
            Long questId = 0L;
            Long userId = 0L;
            doThrow(EntityNotFoundException.class).when(questQueryService).findByIdOrThrow(eq(questId));

            //when
            Runnable call = () -> questCommandService.interactWithDetailQuest(userId, questId, 1L, new DetailInteractRequest());

            //then
            assertThatThrownBy(call::run).isInstanceOf(EntityNotFoundException.class);
        }

        @DisplayName("퀘스트 소유주 검증 로직이 호출된다")
        @Test
        void invokeQuestOwnerCheckMethod() {
            //given
            Long questId = 0L;
            Long userId = 1L;
            Long detailQuestId = 0L;
            DetailInteractRequest mockDto = mock(DetailInteractRequest.class);
            Quest mockQuest = mock(Quest.class);
            doReturn(mockQuest).when(questQueryService).findByIdOrThrow(eq(questId));

            //when
            questCommandService.interactWithDetailQuest(userId, questId, detailQuestId, mockDto);

            //then
            verify(mockQuest, times(1)).checkOwnershipOrThrow(eq(userId));
        }

        @DisplayName("정상 호출일 경우 세부 퀘스트 상호 작용 로직이 호출된다")
        @Test
        void doProcessDetailQuestInteraction() {
            //given
            Long questId = 0L;
            Long userId = 1L;
            Long detailQuestId = 0L;
            DetailInteractRequest mockDto = mock(DetailInteractRequest.class);
            Quest mockQuest = mock(Quest.class);

            doReturn(mockQuest).when(questQueryService).findByIdOrThrow(eq(questId));

            //when
            questCommandService.interactWithDetailQuest(userId, questId, detailQuestId, mockDto);

            //then
            verify(mockQuest, times(1)).interactWithDetailQuest(eq(detailQuestId), eq(mockDto));
        }
    }
}
