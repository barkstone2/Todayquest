package todayquest.item.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import todayquest.item.entity.ItemLog;
import todayquest.item.entity.ItemLogType;
import todayquest.item.repository.ItemLogRepository;
import todayquest.quest.entity.QuestLog;
import todayquest.quest.entity.QuestState;
import todayquest.quest.repository.QuestLogRepository;
import todayquest.quest.service.QuestLogService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;

@DisplayName("아이템 로그 서비스 테스트")
@ExtendWith(MockitoExtension.class)
public class ItemLogServiceTest {

    @InjectMocks
    ItemLogService itemLogService;

    @Mock
    ItemLogRepository itemLogRepository;

    @DisplayName("아이템 로그 단건 저장 테스트")
    @Test
    public void testSaveItemLog() throws Exception {
        //given
        Long rewardId = 1L;
        Long userId = 1L;
        ItemLogType type = ItemLogType.USE;

        //when
        itemLogService.saveItemLogs(rewardId, userId, type);

        //then
        verify(itemLogRepository).save(any(ItemLog.class));
    }


    @DisplayName("아이템 로그 다건 저장 테스트")
    @Test
    public void testSaveItemEarnLogs() throws Exception {
        //given
        Long userId = 1L;
        List<Long> earnIds = List.of(1L, 2L, 3L);

        //when
        itemLogService.saveItemEarnLogs(earnIds, userId);

        //then
        verify(itemLogRepository).saveAll(anyList());
    }

    @DisplayName("아이템 로그 조회 테스트")
    @Test
    public void testGetItemLog() throws Exception {
        //given
        Long userId = 1L;

        //when
        itemLogService.getItemLog(userId);

        //then
        verify(itemLogRepository).getItemAnalytics(userId);
    }

}
