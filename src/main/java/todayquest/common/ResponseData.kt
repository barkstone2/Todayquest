package todayquest.common

import todayquest.exception.ErrorResponse

class ResponseData<T>(
    val data: T? = null,
    val errorResponse: ErrorResponse? = null,
) {
    constructor(data: T) : this(data = data, null)
    constructor(errorResponse: ErrorResponse) : this(null, errorResponse = errorResponse)

    var hasError: Boolean? = errorResponse != null
}