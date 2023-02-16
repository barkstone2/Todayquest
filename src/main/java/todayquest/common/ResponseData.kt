package todayquest.common

import com.fasterxml.jackson.annotation.JsonInclude
import todayquest.exception.ErrorResponse

@JsonInclude(JsonInclude.Include.NON_NULL)
class ResponseData<T>(
    val data: T? = null,
    val errorResponse: ErrorResponse? = null,
) {
    constructor(data: T) : this(data = data, null)
    constructor(errorResponse: ErrorResponse) : this(null, errorResponse = errorResponse)

    val hasError: Boolean = errorResponse != null
}