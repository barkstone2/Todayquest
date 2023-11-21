package dailyquest.search.repository;

import dailyquest.search.document.QuestDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface QuestIndexRepository extends ElasticsearchRepository<QuestDocument, Long> {
}
