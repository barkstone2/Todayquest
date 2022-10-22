package todayquest.item.repository;

import java.util.Map;

public interface ItemLogRepositoryCustom {
    Map<String, Long> getItemAnalytics(Long userId);
}
