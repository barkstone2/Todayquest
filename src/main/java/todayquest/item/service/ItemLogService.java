package todayquest.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import todayquest.item.entity.ItemLog;
import todayquest.item.entity.ItemLogType;
import todayquest.item.repository.ItemLogRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Transactional
@Service
public class ItemLogService {

    private final ItemLogRepository itemLogRepository;


    public Map<String, Long> getItemLog(Long userId) {
        return itemLogRepository.getItemAnalytics(userId);
    }

    public void saveItemEarnLogs(List<Long> earnIds, Long userId) {
        // 아이템 획득 로그 저장
        List<ItemLog> itemEarnLogs = earnIds.stream()
                .map(rewardId -> ItemLog.builder()
                        .rewardId(rewardId)
                        .userId(userId)
                        .type(ItemLogType.EARN)
                        .build()
                )
                .collect(Collectors.toList());
        itemLogRepository.saveAll(itemEarnLogs);
    }

    public void saveItemLogs(Long rewardId, Long userId, ItemLogType type) {
        itemLogRepository.save(ItemLog.builder().rewardId(rewardId).userId(userId).type(type).build());
    }


}
