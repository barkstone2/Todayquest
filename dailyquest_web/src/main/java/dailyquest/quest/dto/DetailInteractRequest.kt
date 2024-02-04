package dailyquest.quest.dto

import org.hibernate.validator.constraints.Range

class DetailInteractRequest(
    @field:Range(min = 1, max = 255, message = "{Range.details.count}")
    val count: Int = 1
)
