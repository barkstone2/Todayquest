package todayquest.item.repository;

import todayquest.item.entity.Item;

import java.util.List;

public interface ItemRepositoryCustom {
    List<Item> findAllByRewardIdsAndUserId(List<Long> ids, Long userId);
}
