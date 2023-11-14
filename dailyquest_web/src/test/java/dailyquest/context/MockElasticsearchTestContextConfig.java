package dailyquest.context;

import dailyquest.search.service.QuestIndexService;
import org.mockito.Answers;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;

@TestConfiguration
public class MockElasticsearchTestContextConfig {

    @MockBean(answer = Answers.RETURNS_DEEP_STUBS)
    QuestIndexService questIndexService;
}
