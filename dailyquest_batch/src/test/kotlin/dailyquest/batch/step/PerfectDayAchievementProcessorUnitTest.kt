package dailyquest.batch.step

import dailyquest.achievement.entity.Achievement
import dailyquest.achievement.entity.AchievementAchieveLog
import dailyquest.perfectday.dto.PerfectDayCount
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.batch.item.function.FunctionItemProcessor

@ExtendWith(MockKExtension::class)
@DisplayName("완벽한 하루 업적 프로세서 유닛 테스트")
class PerfectDayAchievementProcessorUnitTest {

    private lateinit var achievements: List<Achievement>
    private lateinit var perfectDayAchievementProcessor: FunctionItemProcessor<PerfectDayCount, AchievementAchieveLog>
    @RelaxedMockK
    private lateinit var achievement: Achievement
    @RelaxedMockK
    private lateinit var perfectDayCount: PerfectDayCount

    @BeforeEach
    fun init() {
        achievements = listOf(achievement)
        val stepConfig = PerfectDayAchievementStepConfig()
        perfectDayAchievementProcessor = stepConfig.perfectDayAchievementProcessor(achievements)
    }

    @DisplayName("목록에서 조건에 맞는 값이 없으면 처리 결과로 null이 반환된다")
    @Test
    fun `목록에서 조건에 맞는 값이 없으면 처리 결과로 null이 반환된다`() {
        //given
        every { achievement.targetValue } returns 1
        every { perfectDayCount.count } returns 2

        //when
        val result = perfectDayAchievementProcessor.process(perfectDayCount)

        //then
        assertThat(result).isNull()
    }

    @DisplayName("업적 목록에 현재 값과 목표값이 일치하는 업적이 있으면, 해당 업적에 대한 완료 로그를 반환한다")
    @Test
    fun `업적 목록에 현재 값과 목표값이 일치하는 업적이 있으면, 해당 업적에 대한 완료 로그를 반환한다`() {
        //given
        every { achievement.targetValue } returns 1
        every { perfectDayCount.count } returns 1

        //when
        val result = perfectDayAchievementProcessor.process(perfectDayCount)

        //then
        assertThat(result.achievement).isEqualTo(achievement)
    }

    @DisplayName("목록에서 조건에 맞는 값이 여러개면 첫 번째 값이 사용된다")
    @Test
    fun `목록에서 조건에 맞는 값이 여러개면 첫 번째 값이 사용된다`() {
        //given
        every { achievement.targetValue } returns 1
        every { perfectDayCount.count } returns 1

        //when
        val result = perfectDayAchievementProcessor.process(perfectDayCount)

        //then
        verify(exactly = 1) { achievement.targetValue }
    }

}