package dailyquest.config

import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer
import dailyquest.properties.DateFormatProperties
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import java.time.format.DateTimeFormatter

@Configuration
class WebMvcConfig(
    private val dateFormatProperties: DateFormatProperties
) : Jackson2ObjectMapperBuilderCustomizer {
    override fun customize(jacksonObjectMapperBuilder: Jackson2ObjectMapperBuilder) {
        jacksonObjectMapperBuilder.simpleDateFormat(dateFormatProperties.dateTimeFormat)
        jacksonObjectMapperBuilder.serializers(LocalDateSerializer(DateTimeFormatter.ofPattern(dateFormatProperties.dateFormat)))
        jacksonObjectMapperBuilder.serializers(LocalTimeSerializer(DateTimeFormatter.ofPattern(dateFormatProperties.timeFormat)))
        jacksonObjectMapperBuilder.serializers(LocalDateTimeSerializer(DateTimeFormatter.ofPattern(dateFormatProperties.dateTimeFormat)))
    }
}
