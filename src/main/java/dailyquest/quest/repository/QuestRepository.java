package dailyquest.quest.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import dailyquest.quest.entity.Quest;
import dailyquest.quest.entity.QuestState;

@Repository
public interface QuestRepository extends JpaRepository<Quest, Long>, QuestRepositoryCustom {
    @Query("select q from Quest q where q.user.id = :userId and q.state= :state")
    Page<Quest> getQuestsList(@Param("userId") Long userId, @Param("state") QuestState state, Pageable pageable);

}
