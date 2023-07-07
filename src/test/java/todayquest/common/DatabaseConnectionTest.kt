package todayquest.common

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.boot.env.YamlPropertySourceLoader
import org.springframework.core.env.StandardEnvironment
import org.springframework.core.io.ClassPathResource
import java.sql.DriverManager
import java.util.*


@DisplayName("데이터베이스 연결 테스트")
class DatabaseConnectionTest {


    @DisplayName("운영 데이터베이스 연결 테스트")
    @Test
    fun databaseConnectionTest() {

        val env = StandardEnvironment()
        val loader = YamlPropertySourceLoader()
        val resource = ClassPathResource("application-realdb.yml")
        val propertySource = loader.load("application-realdb.yml", resource).first()
        env.propertySources.addFirst(propertySource)

        val url = env.getRequiredProperty("spring.datasource.hikari.jdbc-url")
        val username = env.getRequiredProperty("spring.datasource.hikari.username")
        val password = env.getRequiredProperty("spring.datasource.hikari.password")

        val connection = DriverManager.getConnection(url, username, password)
        connection.close()
    }
}