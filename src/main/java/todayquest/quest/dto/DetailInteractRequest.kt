package todayquest.quest.dto

import org.hibernate.validator.constraints.Range

class DetailInteractRequest(
    count: Int? = null
){
    @Range(min = 1, max = 255, message = "{Range.details.count}")
    var count = count ?: 1
}
