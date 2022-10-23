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
import todayquest.item.entity.Item;
import todayquest.item.service.ItemService;
import todayquest.quest.dto.QuestRequestDto;
import todayquest.quest.dto.QuestResponseDto;
import todayquest.quest.entity.Quest;
import todayquest.quest.entity.QuestDifficulty;
import todayquest.quest.entity.QuestState;
import todayquest.quest.entity.QuestType;
import todayquest.quest.repository.QuestRepository;
import todayquest.quest.repository.QuestRewardRepository;
import todayquest.reward.entity.Reward;
import todayquest.reward.repository.RewardRepository;
import todayquest.user.dto.UserPrincipal;
import todayquest.user.entity.UserInfo;
import todayquest.user.repository.UserRepository;
import todayquest.user.service.UserService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

        //when
        when(questRepository.getQuestsList(any(), any(), any())).thenReturn(new SliceImpl<>(entityList));

        //then
        Slice<QuestResponseDto> result = questService.getQuestList(userId, QuestState.PROCEED, PageRequest.of(0, 9));
        assertThat(result.getSize()).isEqualTo(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("test");
    }

    @DisplayName("퀘스트 목록 테스트_반환값 없을때")
    @Test
    public void testGetListReturnNone() throws Exception {
        //given
        Long userId = 1L;
        List<Quest> entityList = List.of();

        //when
        when(questRepository.getQuestsList(any(), any(), any())).thenReturn(new SliceImpl<>(entityList));

        //then
        Slice<QuestResponseDto> result = questService.getQuestList(userId, QuestState.PROCEED, PageRequest.of(0, 9));
        assertThat(result.getSize()).isEqualTo(0);
    }

    @DisplayName("퀘스트 저장 테스트")
    @Test
    public void testSaveQuest() throws Exception {
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

        //when
        UserInfo findUser = UserInfo.builder().build();
        when(userRepository.getById(any())).thenReturn(findUser);
        when(questRepository.save(any())).thenReturn(Quest.builder().build());

        //then
        questService.saveQuest(dto, 1L);
    }
    @DisplayName("본인의 퀘스트 업데이트 테스트")
    @Test
    public void testQuestUpdateSuccess() throws Exception {
        //given
        Quest entity = Quest.builder()
                .title("test")
                .description("test")
                .state(QuestState.PROCEED)
                .type(QuestType.DAILY)
                .difficulty(QuestDifficulty.easy)
                .isRepeat(true)
                .user(UserInfo.builder().id(1L).build())
                .deadLineDate(LocalDate.now())
                .build();

        //when
        when(questRepository.findById(any())).thenReturn(Optional.ofNullable(entity));
        when(rewardRepository.findAllById(any())).thenReturn(new ArrayList<>());

        //then
        QuestRequestDto dto = QuestRequestDto.builder()
                .title("test")
                .description("test")
                .difficulty(QuestDifficulty.easy)
                .isRepeat(true)
                .deadLineDate(LocalDate.now())
                .deadLineTime(LocalTime.now())
                .build();

        assertThatNoException().isThrownBy(() -> questService.updateQuest(dto, 1L, 1L));
    }

    @DisplayName("다른 유저의 퀘스트 업데이트 테스트")
    @Test
    public void testQuestUpdateFail() throws Exception {
        //given
        Quest entity = Quest.builder()
                .title("test")
                .description("test")
                .state(QuestState.PROCEED)
                .type(QuestType.DAILY)
                .difficulty(QuestDifficulty.easy)
                .isRepeat(true)
                .user(UserInfo.builder().id(1L).build())
                .deadLineDate(LocalDate.now())
                .build();

        //when
        when(questRepository.findById(any())).thenReturn(Optional.ofNullable(entity));

        //then
        QuestRequestDto dto = QuestRequestDto.builder()
                .title("test")
                .description("test")
                .difficulty(QuestDifficulty.easy)
                .isRepeat(true)
                .deadLineDate(LocalDate.now())
                .deadLineTime(LocalTime.now())
                .build();

        assertThatThrownBy(() -> questService.updateQuest(dto, 1L, 2L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage(MessageUtil.getMessage("exception.access.denied", MessageUtil.getMessage("quest")));
    }

    @DisplayName("진행 상태가 아닌 퀘스트 업데이트 테스트")
    @Test
    public void testQuestUpdateStateNotProceed() throws Exception {
        //given
        Quest entity = Quest.builder()
                .title("test")
                .description("test")
                .state(QuestState.COMPLETE)
                .type(QuestType.DAILY)
                .difficulty(QuestDifficulty.easy)
                .isRepeat(true)
                .user(UserInfo.builder().id(1L).build())
                .deadLineDate(LocalDate.now())
                .build();

        //when
        when(questRepository.findById(any())).thenReturn(Optional.ofNullable(entity));

        //then
        QuestRequestDto dto = QuestRequestDto.builder()
                .title("test")
                .description("test")
                .difficulty(QuestDifficulty.easy)
                .isRepeat(true)
                .deadLineDate(LocalDate.now())
                .deadLineTime(LocalTime.now())
                .build();

        assertThatThrownBy(() -> questService.updateQuest(dto, 1L, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(MessageUtil.getMessage("quest.error.update.invalid.state"));
    }

    @DisplayName("퀘스트 정보 조회 실패")
    @Test
    public void testGetQuestInfoFail() throws Exception {
        //given
        Quest entity = Quest.builder()
                .title("test")
                .description("test")
                .state(QuestState.COMPLETE)
                .type(QuestType.DAILY)
                .difficulty(QuestDifficulty.easy)
                .isRepeat(true)
                .user(UserInfo.builder().id(1L).build())
                .deadLineDate(LocalDate.now())
                .build();

        //when
        when(questRepository.findById(any())).thenReturn(Optional.ofNullable(null));

        //then
        QuestRequestDto dto = QuestRequestDto.builder()
                .title("test")
                .description("test")
                .difficulty(QuestDifficulty.easy)
                .isRepeat(true)
                .deadLineDate(LocalDate.now())
                .deadLineTime(LocalTime.now())
                .build();

        assertThatThrownBy(() -> questService.updateQuest(dto, 1L, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(MessageUtil.getMessage("exception.entity.notfound", MessageUtil.getMessage("quest")));
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

        //when
        when(questRepository.findById(any())).thenReturn(Optional.ofNullable(entity));

        //then
        assertThatNoException().isThrownBy(() -> questService.deleteQuest(questId, 1L));
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
        Map<String, Object> attr = new HashMap<>();
        attr.put("level", 1);
        principal.setAttributes(attr);

        int beforeLevel = 1;
        long beforeExp = 99L;
        long beforeGold = 0L;

        UserInfo user = UserInfo.builder()
                .id(userId).level(beforeLevel)
                .gold(beforeGold).exp(beforeExp)
                .build();

        Quest quest = Quest.builder()
                .title("test")
                .description("test")
                .state(QuestState.PROCEED)
                .type(QuestType.DAILY)
                .difficulty(QuestDifficulty.easy)
                .isRepeat(true)
                .user(user)
                .build();

        Reward r1 = Reward.builder().name("r1").id(1L).build();
        Reward r2 = Reward.builder().name("r2").id(2L).build();

        int beforeCount = 10;
        Item i1 = Item.builder().reward(r1).count(beforeCount).build();
        Item i2 = Item.builder().reward(r2).count(1).build();

        when(questRepository.findById(any())).thenReturn(Optional.ofNullable(quest));

        //when
        questService.completeQuest(questId, principal);

        //then
        verify(itemService).saveAllWithDirtyChecking(any(), eq(user));
        verify(userService).earnExpAndGold(user, quest.getDifficulty(), principal);
        verify(questLogService).saveQuestLog(questId, userId, QuestState.COMPLETE);

        assertThat(quest.getState()).isEqualTo(QuestState.COMPLETE);
    }

    @DisplayName("퀘스트 상태 완료로 변경_실패")
    @Test
    public void testQuestCompleteFail() throws Exception {
        //given
        Long questId = 1L;
        UserPrincipal principal = UserPrincipal.builder()
                .userId(1L).build();
        Map<String, Object> attr = new HashMap<>();
        attr.put("level", 1);
        principal.setAttributes(attr);

        int beforeLevel = 1;
        long beforeExp = 99L;
        long beforeGold = 0L;

        UserInfo user = UserInfo.builder()
                .id(1L).level(beforeLevel).gold(beforeGold).exp(beforeExp)
                .build();
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

        //when

        when(questRepository.findById(any())).thenReturn(Optional.ofNullable(quest));

        //then
        assertThatThrownBy(() -> questService.completeQuest(questId, principal))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(MessageUtil.getMessage("quest.error.deleted"));
        assertThat(quest.getState()).isEqualTo(QuestState.DELETE);
    }


    @DisplayName("퀘스트 포기 테스트_성공")
    @Test
    public void testDiscardQuest() throws Exception {
        //given
        Long questId = 1L;

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

        //when
        when(questRepository.findById(any())).thenReturn(Optional.ofNullable(quest));

        //then
        assertThatNoException().isThrownBy(() -> questService.discardQuest(questId, 1L));
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

        //when
        when(questRepository.findById(any())).thenReturn(Optional.ofNullable(quest));

        //then
        assertThatThrownBy(() -> questService.discardQuest(questId, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(MessageUtil.getMessage("quest.error.deleted"));
        assertThat(quest.getState()).isEqualTo(QuestState.DELETE);
    }

}