package dailyquest.context

import com.ninjasquad.springmockk.MockkBean
import dailyquest.properties.SqsQueueProperties
import org.springframework.boot.test.context.TestConfiguration
import software.amazon.awssdk.services.sqs.SqsAsyncClient


@TestConfiguration
class MockSqsClientTestContextConfig {
    @MockkBean
    private lateinit var sqsAsyncClient: SqsAsyncClient
    @MockkBean
    private lateinit var sqsQueueProperties: SqsQueueProperties
}