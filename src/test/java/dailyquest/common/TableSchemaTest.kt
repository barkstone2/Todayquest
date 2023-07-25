package dailyquest.common

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.TestPropertySource


@DisplayName("테이블 구조가 엔티티와 일치하는지 확인")
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@TestPropertySource(properties = ["spring.config.location = classpath:application-realdb.yml"])
class TableSchemaTest {

    @DisplayName("운영 데이터베이스와 엔티티 일치 테스트")
    @Test
    fun test() {

    }
}