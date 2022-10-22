package todayquest.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import todayquest.item.repository.ItemLogRepository;

import java.util.Map;

@RequiredArgsConstructor
@Transactional
@Service
public class ItemLogService {

    private final ItemLogRepository itemLogRepository;


    public Map<String, Long> getItemLog(Long userId) {
        return itemLogRepository.getItemAnalytics(userId);
    }
}
