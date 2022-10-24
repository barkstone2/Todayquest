package todayquest.quest.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.security.access.AccessDeniedException;
import todayquest.common.MessageUtil;
import todayquest.item.service.ItemService;
import todayquest.quest.dto.QuestRequestDto;
import todayquest.quest.dto.QuestResponseDto;
import todayquest.quest.entity.Quest;
import todayquest.quest.entity.QuestDifficulty;
import todayquest.quest.entity.QuestState;
import todayquest.quest.entity.QuestType;
import todayquest.quest.repository.QuestRepository;
import todayquest.quest.repository.QuestRewardRepository;
import todayquest.reward.repository.RewardRepository;
import todayquest.user.dto.UserPrincipal;
import todayquest.user.entity.UserInfo;
import todayquest.user.repository.UserRepository;
import todayquest.user.service.UserService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@DisplayName("퀘스트 서비스 유닛 테스트")
@ExtendWith(MockitoExtension.class)
class QuestServiceTest {

    @InjectMocks QuestService questService;

    // 역할 객체
    @Mock QuestRepository questRepository;
    @Mock UserRepository userRepository;
    @Mock RewardRepository rewardRepository;
    @Mock QuestRewardRepository questRewardRepository;

    // 협력 객체
    @Mock ItemService itemService;
    @Mock UserService userService;
    @Mock QuestLogService questLogService;

    // 기타 처리 용
    @InjectMocks MessageUtil messageUtil;
    @Mock MessageSource messageSource;

    @DisplayName("퀘스트 목록 테스트_반환값 있을때")
    @Test
    public void testGetQuestList() throws Exception {
        //given
        Long userId = 1L;
        QuestState state = QuestState.PROCEED;

        List<Quest> entityList = List.of(
                Quest.builder()
                        .title("test")
                        .description("test")
                        .state(QuestState.PROCEED)
                        .type(QuestType.DAILY)
                        .difficulty(QuestDifficulty.easy)
                        .isRepeat(true)
                        .user(UserInfo.builder().build())
                        .deadLineDate(LocalDate.now())
                        .build());

        when(questRepository.getQuestsList(eq(userId), eq(state), any(PageRequest.class)))
                .thenReturn(new SliceImpl<>(entityList));

        //when
        Slice<QuestResponseDto> result = questService.getQuestList(userId, state, PageRequest.of(0, 9));

        //then
        verify(questRepository).getQuestsList(eq(userId), eq(state), any(PageRequest.class));
        assertThat(result.getSize()).isEqualTo(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("test");
    }

    @DisplayName("퀘스트 목록 테스트_반환값 없을때")
    @Test
    public void testGetListReturnNone() throws Exception {
        //given
        Long userId = 1L;
        QuestState state = QuestState.PROCEED;
        List<Quest> entityList = List.of();

        when(questRepository.getQuestsList(eq(userId), eq(state), any(PageRequest.class)))
                .thenReturn(new SliceImpl<>(entityList));

        //when
        Slice<QuestResponseDto> result = questService.getQuestList(userId, state, PageRequest.of(0, 9));

        //then
        verify(questRepository).getQuestsList(eq(userId), eq(state), any(PageRequest.class));
        assertThat(result.getSize()).isEqualTo(0);
    }

    @DisplayName("퀘스트 저장 테스트")
    @Test
    public void testSaveQuest() throws Exception {
        //given
        Long userId = 1L;
        Long nextSeq = 2L;

        List<Long> rewards = List.of(1L, 2L, 3L);
        QuestRequestDto dto = QuestRequestDto.builder()
                .title("test")
                .description("test")
                .difficulty(QuestDifficulty.easy)
                .isRepeat(true)
                .deadLineDate(LocalDate.now())
                .deadLineTime(LocalTime.now())
                .rewards(rewards)
                .build();

        UserInfo findUser = UserInfo.builder().id(userId).build();
        when(userRepository.getById(any()))
                .thenReturn(findUser);
        when(questRepository.save(any()))
                .thenReturn(Quest.builder().build());
        when(questRepository.getNextSeqByUserId(userId))
                .thenReturn(nextSeq);

        //when
        questService.saveQuest(dto, 1L);

        //then
        verify(userRepository).getById(userId);
        verify(questRepository).getNextSeqByUserId(userId);
        verify(questRepository).save(any(Quest.class));
        verify(rewardRepository).findAllByIdAndUserId(rewards, userId);
        verify(questRewardRepository).saveAll(any());
    }

    @DisplayName("퀘스트 업데이트 성공")
    @Test
    public void testQuestUpdateSuccess() throws Exception {
        //given
        Long questId = 1L;
        Long userId = 1L;

        String title = "test";
        String updateTitle = title + "update";
        Quest entity = Quest.builder()
                .title(title)
                .description("test")
                .state(QuestState.PROCEED)
                .type(QuestType.DAILY)
                .difficulty(QuestDifficulty.easy)
                .isRepeat(true)
                .user(UserInfo.builder().id(userId).build())
                .deadLineDate(LocalDate.now())
                .build();

        when(questRepository.findById(any()))
                .thenReturn(Optional.ofNullable(entity));
        when(rewardRepository.findAllById(any()))
                .thenReturn(new ArrayList<>());

        List<Long> rewards = List.of(1L, 2L, 3L);

        QuestRequestDto dto = QuestRequestDto.builder()
                .title(updateTitle)
                .description("test")
                .difficulty(QuestDifficulty.easy)
                .isRepeat(true)
                .deadLineDate(LocalDate.now())
                .deadLineTime(LocalTime.now())
                .rewards(rewards)
                .build();

        //when
        questService.updateQuest(dto, questId, userId);

        //then
        verify(questRepository).findById(questId);
        verify(rewardRepository).findAllById(rewards);
        verify(questRewardRepository).saveAll(any());

        assertThat(entity.getTitle()).isEqualTo(updateTitle);
    }

    @DisplayName("다른 유저의 퀘스트 업데이트 테스트")
    @Test
    public void testQuestUpdateFail() throws Exception {
        //given
        Long questId = 1L;
        Long userId = 1L;
        Long anotherUserId = 2L;

        String title = "test";
        String updateTitle = title + "update";

        Quest entity = Quest.builder()
                .title(title)
                .description("test")
                .state(QuestState.PROCEED)
                .type(QuestType.DAILY)
                .difficulty(QuestDifficulty.easy)
                .isRepeat(true)
                .user(UserInfo.builder().id(userId).build())
                .deadLineDate(LocalDate.now())
                .build();
        when(questRepository.findById(any()))
                .thenReturn(Optional.ofNullable(entity));

        QuestRequestDto dto = QuestRequestDto.builder()
                .title(updateTitle)
                .description("test")
                .difficulty(QuestDifficulty.easy)
                .isRepeat(true)
                .deadLineDate(LocalDate.now())
                .deadLineTime(LocalTime.now())
                .build();


        //when
        ThrowingCallable tc = () -> questService.updateQuest(dto, questId, anotherUserId);

        //then
        assertThatThrownBy(tc)
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage(MessageUtil.getMessage("exception.access.denied", MessageUtil.getMessage("quest")));

        assertThat(entity.getTitle()).isEqualTo(title);
        assertThat(entity.getTitle()).isNotEqualTo(updateTitle);

        verify(questRepository).findById(questId);
        verifyNoInteractions(rewardRepository);
        verifyNoInteractions(questRewardRepository);
    }

    @DisplayName("진행 상태가 아닌 퀘스트 업데이트 테스트")
    @Test
    public void testQuestUpdateStateNotProceed() throws Exception {
        //given
        Long questId = 1L;
        Long userId = 1L;

        Quest entity = Quest.builder()
                .title("test")
                .description("test")
                .state(QuestState.COMPLETE)
                .type(QuestType.DAILY)
                .difficulty(QuestDifficulty.easy)
                .isRepeat(true)
                .user(UserInfo.builder().id(userId).build())
                .build();

        when(questRepository.findById(any()))
                .thenReturn(Optional.ofNullable(entity));

        QuestRequestDto dto = QuestRequestDto.builder()
                .title("test")
                .description("test")
                .difficulty(QuestDifficulty.easy)
                .isRepeat(true)
                .build();
        //when
        ThrowingCallable tc = () -> questService.updateQuest(dto, questId, userId);


        //then
        assertThatThrownBy(tc)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(MessageUtil.getMessage("quest.error.update.invalid.state"));

        verify(questRepository).findById(questId);
        verifyNoInteractions(rewardRepository);
        verifyNoInteractions(questRewardRepository);
    }

    @DisplayName("퀘스트 업데이트 정보 조회 실패")
    @Test
    public void testGetQuestInfoFail() throws Exception {
        //given
        Long questId = 1L;
        Long userId = 1L;
        Quest entity = Quest.builder()
                .title("test")
                .description("test")
                .state(QuestState.COMPLETE)
                .type(QuestType.DAILY)
                .difficulty(QuestDifficulty.easy)
                .isRepeat(true)
                .user(UserInfo.builder().id(userId).build())
                .deadLineDate(LocalDate.now())
                .build();

        when(questRepository.findById(any()))
                .thenReturn(Optional.ofNullable(null));

        QuestRequestDto dto = QuestRequestDto.builder()
                .title("test")
                .description("test")
                .difficulty(QuestDifficulty.easy)
                .isRepeat(true)
                .deadLineDate(LocalDate.now())
                .deadLineTime(LocalTime.now())
                .build();

        //when
        ThrowingCallable tc = () -> questService.updateQuest(dto, questId, userId);

        //then
        assertThatThrownBy(tc)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(MessageUtil.getMessage("exception.entity.notfound", MessageUtil.getMessage("quest")));

        verify(questRepository).findById(questId);
        verifyNoInteractions(rewardRepository);
        verifyNoInteractions(questRewardRepository);
    }

    @DisplayName("퀘스트 삭제 테스트")
    @Test
    public void testQuestDeleteSuccess() throws Exception {
        //given
        Long questId = 1L;
        Quest entity = Quest.builder()
                .title("test")
                .description("test")
                .state(QuestState.PROCEED)
                .type(QuestType.DAILY)
                .difficulty(QuestDifficulty.easy)
                .isRepeat(true)
                .deadLineDate(LocalDate.now())
                .user(UserInfo.builder().id(1L).build())
                .build();

        when(questRepository.findById(any()))
                .thenReturn(Optional.ofNullable(entity));

        //when
        questService.deleteQuest(questId, 1L);

        //then
        verify(questRepository).findById(questId);
        assertThat(entity.getState()).isEqualTo(QuestState.DELETE);
    }


    @DisplayName("퀘스트 상태 완료로 변경_성공")
    @Test
    public void testQuestComplete() throws Exception {
        //given
        Long questId = 1L;
        Long userId = 1L;
        UserPrincipal principal = UserPrincipal.builder()
                .userId(userId).build();

        UserInfo user = UserInfo.builder().id(userId).build();

        Quest quest = Quest.builder()
                .title("test")
                .description("test")
                .state(QuestState.PROCEED)
                .type(QuestType.DAILY)
                .difficulty(QuestDifficulty.easy)
                .isRepeat(true)
                .user(user)
                .build();

        when(questRepository.findById(any()))
                .thenReturn(Optional.ofNullable(quest));

        //when
        questService.completeQuest(questId, principal);

        //then
        verify(itemService).saveAllWithDirtyChecking(any(), eq(user));
        verify(userService).earnExpAndGold(user, quest.getDifficulty(), principal);
        verify(questLogService).saveQuestLog(questId, userId, QuestState.COMPLETE);

        assertThat(quest.getState()).isEqualTo(QuestState.COMPLETE);
    }

    @DisplayName("퀘스트 상태 완료로 변경_삭제된 퀘스트")
    @Test
    public void testQuestCompleteFail() throws Exception {
        //given
        Long questId = 1L;
        UserPrincipal principal = UserPrincipal.builder()
                .userId(1L).build();

        Quest quest = Quest.builder()
                .title("test")
                .description("test")
                .state(QuestState.DELETE)
                .type(QuestType.DAILY)
                .difficulty(QuestDifficulty.easy)
                .isRepeat(true)
                .deadLineDate(LocalDate.now())
                .user(UserInfo.builder().id(1L).build())
                .build();

        when(questRepository.findById(any()))
                .thenReturn(Optional.ofNullable(quest));

        //when
        ThrowingCallable tc = () -> questService.completeQuest(questId, principal);

        //then
        assertThatThrownBy(tc)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(MessageUtil.getMessage("quest.error.deleted"));
        verifyNoMoreInteractions(userService);
        verifyNoInteractions(itemService);
        verifyNoInteractions(questLogService);
        assertThat(quest.getState()).isEqualTo(QuestState.DELETE);
    }


    @DisplayName("퀘스트 포기 테스트_성공")
    @Test
    public void testDiscardQuest() throws Exception {
        //given
        Long questId = 1L;
        Long userId = 1L;

        Quest quest = Quest.builder()
                .title("test")
                .description("test")
                .state(QuestState.PROCEED)
                .type(QuestType.DAILY)
                .difficulty(QuestDifficulty.easy)
                .isRepeat(true)
                .deadLineDate(LocalDate.now())
                .user(UserInfo.builder().id(1L).build())
                .build();
        when(questRepository.findById(any()))
                .thenReturn(Optional.ofNullable(quest));

        //when
        questService.discardQuest(questId, userId);

        //then
        verify(questLogService).saveQuestLog(questId, userId, QuestState.DISCARD);
        assertThat(quest.getState()).isEqualTo(QuestState.DISCARD);
    }

    @DisplayName("퀘스트 포기 테스트_실패")
    @Test
    public void testDiscardQuestFail() throws Exception {
        //given
        Long questId = 1L;

        Quest quest = Quest.builder()
                .title("test")
                .description("test")
                .state(QuestState.DELETE)
                .type(QuestType.DAILY)
                .difficulty(QuestDifficulty.easy)
                .isRepeat(true)
                .deadLineDate(LocalDate.now())
                .user(UserInfo.builder().id(1L).build())
                .build();

        when(questRepository.findById(any()))
                .thenReturn(Optional.ofNullable(quest));

        //when
        ThrowingCallable tc = () -> questService.discardQuest(questId, 1L);

        //then
        assertThatThrownBy(tc)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(MessageUtil.getMessage("quest.error.deleted"));
        verifyNoInteractions(questLogService);
        assertThat(quest.getState()).isEqualTo(QuestState.DELETE);
    }

}