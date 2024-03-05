package dailyquest.notification.repository

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import dailyquest.notification.entity.Notification
import dailyquest.notification.entity.NotificationType
import dailyquest.user.entity.ProviderType
import dailyquest.user.entity.UserInfo
import dailyquest.user.repository.UserRepository
import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest

@DisplayName("알림 리포지토리 유닛 테스트")
@DataJpaTest
class NotificationRepositoryUnitTest {

    @Autowired
    private lateinit var notificationRepository: NotificationRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var entityManager: EntityManager

    private lateinit var savedUser: UserInfo
    private val om = ObjectMapper().registerKotlinModule()

    @BeforeEach
    fun init() {
        val user = UserInfo("user", "user", ProviderType.GOOGLE)
        savedUser = userRepository.save(user)
    }

    @DisplayName("엔티티 저장 시 오류가 발생하지 않는다")
    @Test
    fun `엔티티 저장 시 오류가 발생하지 않는다`() {
        //given
        val notification = Notification.of(NotificationType.ACHIEVEMENT_ACHIEVE, savedUser, "notification title")

        //when
        //then
        assertDoesNotThrow { notificationRepository.save(notification) }
    }

    @DisplayName("엔티티 저장 시 메타데이터 json 값을 저장해도 오류가 발생하지 않는다")
    @Test
    fun `엔티티 저장 시 메타데이터 json 값을 저장해도 오류가 발생하지 않는다`() {
        //given
        val metadata = mapOf("meta1" to "1", "meta2" to 3, "meta4" to mapOf("a" to "b"))
        val notification = Notification.of(
            NotificationType.ACHIEVEMENT_ACHIEVE,
            savedUser,
            "notification title",
            "",
            om.writeValueAsString(metadata)
        )

        //when
        //then
        assertDoesNotThrow { notificationRepository.save(notification) }
    }

    @DisplayName("메타데이터 조회 시 json을 맵으로 변환 가능하다")
    @Test
    fun `메타데이터 조회 시 json을 맵으로 변환 가능하다`() {
        //given
        val metadata = mutableMapOf<String, String>()
        val pairs = listOf("key1" to "1", "key2" to 2)
        pairs.forEach { metadata[it.first] = it.second.toString() }

        val notification = Notification.of(NotificationType.ACHIEVEMENT_ACHIEVE, savedUser, "notification title", "", om.writeValueAsString(metadata))
        val savedNotification = notificationRepository.saveAndFlush(notification)
        entityManager.clear()
        val foundNotification = notificationRepository.findById(savedNotification.id).get()

        //when
        val metadataMap: Map<String, String> = om.readValue(foundNotification.metadata, jacksonTypeRef())

        //then
        pairs.forEach { assertThat(metadataMap[it.first]).isEqualTo(it.second.toString()) }
    }

    @DisplayName("알림 업데이트 시 오류가 발생하지 않는다")
    @Test
    fun `알림 업데이트 시 오류가 발생하지 않는다`() {
        //given
        val notification = Notification.of(NotificationType.ACHIEVEMENT_ACHIEVE, savedUser, "notification title")
        val savedNotification = notificationRepository.saveAndFlush(notification)
        entityManager.clear()
        val foundNotification = notificationRepository.findById(savedNotification.id).get()

        //when
        foundNotification.confirmNotification()
        foundNotification.deleteNotification()

        //then
        assertDoesNotThrow { notificationRepository.saveAndFlush(foundNotification) }
    }
}