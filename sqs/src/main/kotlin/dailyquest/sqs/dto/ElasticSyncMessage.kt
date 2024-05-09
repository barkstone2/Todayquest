package dailyquest.sqs.dto

import org.springframework.util.DigestUtils
import java.io.Serializable

class ElasticSyncMessage private constructor(
    val requestType: ElasticSyncRequestType,
    val documentId: Long,
    val documentJson: String,
): Serializable {

    fun createUniqueId(): String {
        val jsonToMd5 = DigestUtils.md5DigestAsHex(documentJson.toByteArray())
        return requestType.name + jsonToMd5
    }

    companion object {
        @JvmStatic
        fun of(requestType: ElasticSyncRequestType, documentId: Long, documentJson: String = ""): ElasticSyncMessage {
            return ElasticSyncMessage(requestType, documentId, documentJson)
        }
    }
}