package todayquest.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import todayquest.item.entity.Item;

import java.util.List;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long>, ItemRepositoryCustom {
    List<Item> findByUserIdAndCountIsNot(Long userId, int count);
    Optional<Item> findByIdAndUserId(Long itemId, Long userId);
}
