package dailyquest.sqs.service

import dailyquest.properties.SqsQueueProperties
import dailyquest.sqs.dto.ElasticSyncMessage
import io.awspring.cloud.sqs.operations.SqsTemplate
import org.springframework.stereotype.Service

@Service
class SqsService(
    private val sqsTemplate: SqsTemplate,
    private val sqsQueueProperties: SqsQueueProperties
) {

    fun publishRegisterMessage(achievementId: Long) {
        sqsTemplate.sendAsync {
            it.queue(sqsQueueProperties.batchJobQueueUrl)
                .payload(achievementId)
                .messageGroupId(achievementId.toString())
                .messageDeduplicationId(achievementId.toString())
        }
    }

    fun publishElasticSyncMessage(elasticSyncMessage: ElasticSyncMessage) {
        val uniqueId = elasticSyncMessage.createUniqueId()
        sqsTemplate.sendAsync {
            it.queue(sqsQueueProperties.elasticSyncQueueUrl)
                .payload(elasticSyncMessage)
                .messageGroupId(uniqueId)
                .messageDeduplicationId(uniqueId)
        }
    }
}