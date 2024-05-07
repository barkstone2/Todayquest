package dailyquest.quest.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import dailyquest.quest.entity.Quest;
import dailyquest.quest.entity.QuestState;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface QuestRepository extends JpaRepository<Quest, Long>, QuestRepositoryCustom {

    @Query("select q from Quest q where q.id in :searchedIds order by q.id desc")
    Page<Quest> getSearchedQuests(@Param("searchedIds") List<Long> searchedIds, Pageable pageable);

    @Query("select q from Quest q where q.userId = :userId and q.state= :state and (q.createdDate between :prevReset and :nextReset or q.deadLine > now())")
    List<Quest> getCurrentQuests(@Param("userId") Long userId, @Param("state") QuestState state, @Param("prevReset") LocalDateTime prevReset, @Param("nextReset") LocalDateTime nextReset);

    @Nullable
    Quest findByIdAndUserId(Long questId, Long userId);
}
