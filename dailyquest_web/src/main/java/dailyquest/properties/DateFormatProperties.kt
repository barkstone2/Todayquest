package dailyquest.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.data.elasticsearch.annotations.DateFormat

@ConfigurationProperties(prefix = "http.response")
class DateFormatProperties(
    val dateFormat: String = DateFormat.date.pattern,
    val timeFormat: String = DateFormat.hour_minute_second.pattern,
    val dateTimeFormat: String = "${DateFormat.date.pattern} ${DateFormat.hour_minute_second.pattern}",
)