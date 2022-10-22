package todayquest.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import todayquest.item.entity.ItemLog;

public interface ItemLogRepository extends JpaRepository<ItemLog, Long>, ItemLogRepositoryCustom {
}
