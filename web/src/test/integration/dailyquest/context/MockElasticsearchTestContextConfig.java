package dailyquest.context;

import com.ninjasquad.springmockk.MockkBean;
import dailyquest.search.repository.QuestIndexRepository;
import dailyquest.search.service.QuestIndexService;
import org.mockito.Answers;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;

@TestConfiguration
public class MockElasticsearchTestContextConfig {

    @MockkBean(relaxed = true)
    @MockBean(answer = Answers.RETURNS_SMART_NULLS)
    QuestIndexService questIndexService;

    @MockkBean(relaxed = true)
    @MockBean(answer = Answers.RETURNS_SMART_NULLS)
    QuestIndexRepository questIndexRepository;
}
