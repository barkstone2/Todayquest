package dailyquest.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "aws.sqs.queue")
class SqsQueueProperties(
    val batchJobQueueUrl: String = "",
    val elasticSyncQueueUrl: String = "",
)