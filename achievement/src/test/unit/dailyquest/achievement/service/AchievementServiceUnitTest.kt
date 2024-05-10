package dailyquest.achievement.service

import dailyquest.achievement.dto.AchievementAchieveRequest
import dailyquest.achievement.dto.AchievementResponse
import dailyquest.achievement.dto.AchievementSaveRequest
import dailyquest.achievement.dto.AchievementUpdateRequest
import dailyquest.achievement.entity.Achievement
import dailyquest.achievement.entity.AchievementType
import dailyquest.achievement.entity.AchievementType.*
import dailyquest.achievement.repository.AchievementRepository
import dailyquest.notification.service.NotificationService
import dailyquest.properties.AchievementPageSizeProperties
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.context.MessageSource
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull

@ExtendWith(MockKExtension::class)
@DisplayName("업적 서비스 유닛 테스트")
class AchievementServiceUnitTest {
    @RelaxedMockK
    private lateinit var achievementRepository: AchievementRepository
    @RelaxedMockK
    private lateinit var achievementPageSizeProperties: AchievementPageSizeProperties
    @RelaxedMockK
    private lateinit var achieveLogCommandService: AchievementAchieveLogCommandService
    @RelaxedMockK
    private lateinit var achieveLogQueryService: AchievementAchieveLogQueryService
    @RelaxedMockK
    private lateinit var notificationService: NotificationService
    @RelaxedMockK
    private lateinit var messageSource: MessageSource
    @InjectMockKs
    private lateinit var achievementService: AchievementService

    @DisplayName("getAchievedAchievements 호출 시")
    @Nested
    inner class TestGetAchievedAchievements {
        @DisplayName("인자의 page와 프로퍼티 클래스의 pageSize로 목록을 요청한다")
        @Test
        fun `인자의 page와 프로퍼티 클래스의 pageSize로 목록을 요청한다`() {
            //given
            val userId = 1L
            val page = 1
            val pageSize = 1
            every { achievementPageSizeProperties.size } returns pageSize
            val pageRequest = PageRequest.of(page, pageSize)

            //when
            achievementService.getAchievedAchievements(userId, page)

            //then
            verify {
                achieveLogQueryService.getAchievedAchievements(eq(userId), eq(pageRequest))
            }
        }
    }

    @DisplayName("getNotAchievedAchievements 호출 시")
    @Nested
    inner class TestGetNotAchievedAchievements {
        @DisplayName("인자의 page와 프로퍼티 클래스의 pageSize로 목록을 요청한다")
        @Test
        fun `인자의 page와 프로퍼티 클래스의 pageSize로 목록을 요청한다`() {
            //given
            val userId = 1L
            val page = 1
            val pageSize = 1
            every { achievementPageSizeProperties.size } returns pageSize
            val pageRequest = PageRequest.of(page, pageSize)

            //when
            achievementService.getNotAchievedAchievements(userId, page)

            //then
            verify {
                achievementRepository.getNotAchievedAchievements(eq(userId), eq(pageRequest))
            }
        }
    }

    @DisplayName("checkAndAchieveAchievement 호출 시")
    @Nested
    inner class TestCheckAndAchieveAchievement {
        @DisplayName("리포지토리 반환 결과가 null이면 업적이 달성되지 않는다")
        @Test
        fun `리포지토리 반환 결과가 null이면 업적이 달성되지 않는다`() {
            //given
            every { achievementRepository.findNotAchievedAchievement(any(), any()) } returns null
            val achieveRequest = mockk<AchievementAchieveRequest>(relaxed = true)

            //when
            achievementService.checkAndAchieveAchievement(achieveRequest)
            
            //then
            verify(inverse = true) {
                achieveLogCommandService.saveAchieveLog(any(), any())
            }
        }

        @DisplayName("리포지토리 반환 결과가 null이 아니고 달성 가능 여부가 true면 업적 달성 로그를 저장한다")
        @Test
        fun `리포지토리 반환 결과가 null이 아니고 달성 가능 여부가 true면 업적 달성 로그를 저장한다`() {
            //given
            val achievementId = 1L
            val achievement: Achievement = mockk(relaxed = true)
            every { achievement.id } returns achievementId
            every { achievement.canAchieve(any()) } returns true
            every { achievementRepository.findNotAchievedAchievement(any(), any()) } returns achievement
            val achieveRequest = mockk<AchievementAchieveRequest>(relaxed = true)

            //when
            achievementService.checkAndAchieveAchievement(achieveRequest)

            //then
            verify {
                achieveLogCommandService.saveAchieveLog(eq(achievementId), any())
            }
        }

        @DisplayName("리포지토리 반환 결과가 null이 아니고 달성 가능 여부가 false면 달성 로그를 저장하지 않는다")
        @Test
        fun `리포지토리 반환 결과가 null이 아니고 달성 가능 여부가 false면 달성 로그를 저장하지 않는다`() {
            //given
            val achievement: Achievement = mockk(relaxed = true)
            every { achievement.canAchieve(any()) } returns false
            every { achievementRepository.findNotAchievedAchievement(any(), any()) } returns achievement
            val achieveRequest = mockk<AchievementAchieveRequest>(relaxed = true)

            //when
            achievementService.checkAndAchieveAchievement(achieveRequest)

            //then
            verify(inverse = true) {
                achieveLogCommandService.saveAchieveLog(any(), any())
            }
        }
    }

    @DisplayName("saveAchievement 호출시")
    @Nested
    inner class TestSaveAchievement {
        @RelaxedMockK
        private lateinit var saveRequest: AchievementSaveRequest

        @BeforeEach
        fun init() {
            every { achievementRepository.save(any()) } answers { nothing }
        }

        @DisplayName("중복 검사 결과가 true면 예외를 던진다")
        @Test
        fun `중복 검사 결과가 true면 예외를 던진다`() {
            //given
            every { achievementRepository.existsByTypeAndTargetValue(any(), any()) } returns true

            //when
            //then
            assertThrows<IllegalStateException> { achievementService.saveAchievement(saveRequest) }
        }

        @DisplayName("중복 검사 결과가 false면 업적을 저장한다")
        @Test
        fun `중복 검사 결과가 false면 업적을 저장한다`() {
            //given
            every { achievementRepository.existsByTypeAndTargetValue(any(), any()) } returns false

            //when
            achievementService.saveAchievement(saveRequest)

            //then
            verify { achievementRepository.save(any()) }
        }
    }

    @DisplayName("updateAchievement 호출 시")
    @Nested
    inner class TestUpdateAchievement {
        @DisplayName("업적 조회 결과가 null이 아니면 업데이트 로직이 호출된다")
        @Test
        fun `업적 조회 결과가 null이 아니면 업데이트 로직이 호출된다`() {
            //given
            val achievement = mockk<Achievement>(relaxed = true)
            every { achievementRepository.findByIdOrNull(any()) } returns achievement
            val updateRequest = mockk<AchievementUpdateRequest>(relaxed = true)

            //when
            achievementService.updateAchievement(1L, updateRequest)

            //then
            verify { achievement.updateAchievement(eq(updateRequest)) }
        }
    }

    @DisplayName("inactivateAchievement 호출 시")
    @Nested
    inner class TestInactivateAchievement {
        @DisplayName("업적 조회 결과가 null이 아니면 비활성화 로직이 호출된다")
        @Test
        fun `업적 조회 결과가 null이 아니면 비활성화 로직이 호출된다`() {
            //given
            val achievement = mockk<Achievement>(relaxed = true)
            every { achievementRepository.findByIdOrNull(any()) } returns achievement

            //when
            achievementService.inactivateAchievement(1L)

            //then
            verify { achievement.inactivateAchievement() }
        }
    }

    @DisplayName("activateAchievement 호출 시")
    @Nested
    inner class TestActivateAchievement {
        @DisplayName("업적 조회 결과가 null이 아니면 활성화 로직이 호출된다")
        @Test
        fun `업적 조회 결과가 null이 아니면 활성화 로직이 호출된다`() {
            //given
            val achievement = mockk<Achievement>(relaxed = true)
            every { achievementRepository.findByIdOrNull(any()) } returns achievement

            //when
            achievementService.activateAchievement(1L)

            //then
            verify { achievement.activateAchievement() }
        }
    }

    @DisplayName("getAllAchievementsGroupByType 호출 시")
    @Nested
    inner class TestGetAllAchievementsGroupByType {
        @BeforeEach
        fun init() {
            val achievementResponse = mockk<AchievementResponse>()
            mockkObject(AchievementResponse)
            every { AchievementResponse.from(any<Achievement>()) } returns achievementResponse
        }

        @DisplayName("리포지토리 반환 결과가 없으면 각 타입에 대해 빈 리스트가 담겨 반환된다")
        @Test
        fun `리포지토리 반환 결과가 없으면 각 타입에 대해 빈 리스트가 담겨 반환된다`() {
            //given
            every { achievementRepository.getAllByOrderByTypeAscTargetValueAsc() } returns emptyList()

            //when
            val result = achievementService.getAllAchievementsGroupByType()

            //then
            AchievementType.values().forEach {
                assertThat(result[it]).isNotNull.isEmpty()
            }
        }
        
        @DisplayName("리포지토리 반환 결과가 있으면 타입 별로 맵에 담겨 반환된다")
        @Test
        fun `리포지토리 반환 결과가 있으면 타입 별로 맵에 담겨 반환된다`() {
            //given
            val returnedTypes = listOf(QUEST_REGISTRATION, QUEST_COMPLETION, QUEST_CONTINUOUS_COMPLETION)
            val achievement = mockk<Achievement>()
            every { achievement.type } returnsMany returnedTypes
            every { achievementRepository.getAllByOrderByTypeAscTargetValueAsc() } returns returnedTypes.map { achievement }

            //when
            val result = achievementService.getAllAchievementsGroupByType()

            //then
            returnedTypes.forEach {
                assertThat(result[it]).isNotEmpty
            }
        }
        
        @DisplayName("동일한 타입에 대한 반환 결과가 여러개라면 해당 타입 리스트에 모두 담겨 반환된다")
        @Test
        fun `동일한 타입에 대한 반환 결과가 여러개라면 해당 타입 리스트에 모두 담겨 반환된다`() {
            //given
            val achievement = mockk<Achievement>()
            val hasMultiValueType = QUEST_REGISTRATION
            every { achievement.type } returns hasMultiValueType
            val size = 3
            every { achievementRepository.getAllByOrderByTypeAscTargetValueAsc() } returns List(size) { achievement }

            //when
            val result = achievementService.getAllAchievementsGroupByType()

            //then
            assertThat(result[hasMultiValueType]).hasSize(size)
        }
    }
}