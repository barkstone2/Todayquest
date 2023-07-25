package dailyquest.quest.repository;

public interface QuestRepositoryCustom {

    Long getNextSeqByUserId(Long userId);
}
