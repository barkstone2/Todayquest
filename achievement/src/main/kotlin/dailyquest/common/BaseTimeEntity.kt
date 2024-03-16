package dailyquest.common

import jakarta.persistence.EntityListeners
import jakarta.persistence.MappedSuperclass
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@EntityListeners(AuditingEntityListener::class)
@MappedSuperclass
class BaseTimeEntity: CreatedTimeEntity() {
    @LastModifiedDate
    var lastModifiedDate: LocalDateTime = LocalDateTime.now()
}