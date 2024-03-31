package dailyquest.perfectday.repository

import dailyquest.log.perfectday.entity.PerfectDayLog
import dailyquest.perfectday.dto.PerfectDayCount
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.data.domain.Pageable
import java.time.LocalDate

@DataJpaTest
class PerfectDayRepositoryUnitTest {

    @Autowired
    private lateinit var perfectDayLogRepository: PerfectDayLogRepository

    @DisplayName("유저별 카운트 요청 시 PerfectDayCount Dto 목록이 반환된다")
    @Test
    fun `유저별 카운트 요청 시 PerfectDayCount Dto 목록이 반환된다`() {
        //given
        val userIds = listOf(1L)
        perfectDayLogRepository.save(PerfectDayLog(1L, LocalDate.now()))

        //when
        val results = perfectDayLogRepository.countByUserIds(userIds, Pageable.unpaged())

        //then
        assertThat(results).hasOnlyElementsOfTypes(PerfectDayCount::class.java)
    }

    @DisplayName("유저별 카운트 요청 시 요청한 유저의 카운트만 조회된다")
    @Test
    fun `유저별 카운트 요청 시 요청한 유저의 카운트만 조회된다`() {
        //given
        val userIds = listOf(1L, 2L)
        userIds.forEach { perfectDayLogRepository.save(PerfectDayLog(it, LocalDate.now())) }
        perfectDayLogRepository.save(PerfectDayLog(3L, LocalDate.now()))

        //when
        val results = perfectDayLogRepository.countByUserIds(userIds, Pageable.unpaged())

        //then
        assertThat(results).allMatch { userIds.contains(it.userId) }
    }

    @DisplayName("유저별 카운트 요청 시 요청된 유저에 대해 등록된 로그가 없으면 해당 유저 데이터는 생략된다")
    @Test
    fun `유저별 카운트 요청 시 요청된 유저에 대해 등록된 로그가 없으면 해당 유저 데이터는 생략된다`() {
        //given
        val userIds = listOf(1L)

        //when
        val results = perfectDayLogRepository.countByUserIds(userIds, Pageable.unpaged())

        //then
        assertThat(results).isEmpty()
    }
}