package dailyquest.common

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDate

class TimeUtilUnitTest {

    @DisplayName("분기 시작일 조회 시")
    @Nested
    inner class GetQuarterStartDate {

        @DisplayName("요청 날짜가 해당 년도의 첫 월요일보다 빠를 경우 전년도 마지막 분기 시작일을 반환한다")
        @Test
        fun `요청 날짜가 해당 년도의 첫 월요일보다 빠를 경우 전년도 마지막 분기 시작일을 반환한다`() {
            //given
            // 21년도의 첫 월요일은 21년 1월 4일
            val date = LocalDate.of(2021, 1, 3)

            //when
            val firstDayOfQuarter = date.firstDayOfQuarter()

            //then
            assertThat(firstDayOfQuarter).isBefore(date)
        }

        @DisplayName("요청 날짜가 해당 년도의 첫 월요일일 경우 해당 날짜가 그대로 반환된다")
        @Test
        fun `요청 날짜가 해당 년도의 첫 월요일일 경우 해당 날짜가 그대로 반환된다`() {
            //given

            // 21년도의 첫 월요일은 21년 1월 4일
            val date = LocalDate.of(2021, 1, 4)

            //when
            val firstDayOfQuarter = date.firstDayOfQuarter()

            //then
            assertThat(firstDayOfQuarter).isEqualTo(date)
        }

        @DisplayName("요청 날짜가 분기 시작일 이후라면, 알맞은 분기 시작일이 반환된다")
        @Test
        fun `요청 날짜가 분기 시작일 이후라면, 알맞은 분기 시작일이 반환된다`() {
            //given
            // 21년도의 첫 월요일은 21년 1월 4일
            val date = LocalDate.of(2021, 1, 5)

            //when
            val firstDayOfQuarter = date.firstDayOfQuarter()

            //then
            assertThat(firstDayOfQuarter).isBefore(date)
        }

        @DisplayName("13주 단위로 구분된 분기 시작일이 반환된다")
        @Test
        fun `13주 단위로 구분된 분기 시작일이 반환된다`() {
            //given
            // 21년도의 첫 월요일은 21년 1월 4일
            val firstQuarterStart = LocalDate.of(2021, 1, 4)
            val secondQuarterStart = firstQuarterStart.plusWeeks(13)
            val thirdQuarterStart = secondQuarterStart.plusWeeks(13)
            val fourthQuarterStart = thirdQuarterStart.plusWeeks(13)

            val firstQuarterEnd = secondQuarterStart.minusDays(1)
            val secondQuarterEnd = thirdQuarterStart.minusDays(1)
            val thirdQuarterEnd = fourthQuarterStart.minusDays(1)
            val fourthQuarterEnd = fourthQuarterStart.plusWeeks(13).minusDays(1)

            //when
            val firstQuarterStart1 = firstQuarterStart.firstDayOfQuarter()
            val secondQuarterStart1 = secondQuarterStart.firstDayOfQuarter()
            val thirdQuarterStart1 = thirdQuarterStart.firstDayOfQuarter()
            val fourthQuarterStart1 = fourthQuarterStart.firstDayOfQuarter()

            val firstQuarterStart2 = firstQuarterEnd.firstDayOfQuarter()
            val secondQuarterStart2 = secondQuarterEnd.firstDayOfQuarter()
            val thirdQuarterStart2 = thirdQuarterEnd.firstDayOfQuarter()
            val fourthQuarterStart2 = fourthQuarterEnd.firstDayOfQuarter()

            //then
            assertThat(firstQuarterStart1).isEqualTo(firstQuarterStart2)
            assertThat(secondQuarterStart1).isEqualTo(secondQuarterStart2)
            assertThat(thirdQuarterStart1).isEqualTo(thirdQuarterStart2)
            assertThat(fourthQuarterStart1).isEqualTo(fourthQuarterStart2)

            assertThat(firstQuarterStart1).isBefore(secondQuarterStart1)
            assertThat(secondQuarterStart1).isBefore(thirdQuarterStart1)
            assertThat(thirdQuarterStart1).isBefore(fourthQuarterStart1)
        }

    }

    @DisplayName("분기 종료일 조회 시")
    @Nested
    inner class GetQuarterEndDate {

        @DisplayName("요청 날짜가 해당 년도의 첫 월요일보다 빠를 경우 첫 월요일 전날을 반환한다")
        @Test
        fun `요청 날짜가 해당 년도의 첫 월요일보다 빠를 경우 첫 월요일 전날을 반환한다`() {
            //given
            // 21년도의 첫 월요일은 21년 1월 4일
            val date = LocalDate.of(2021, 1, 3)

            //when
            val lastDayOfQuarter = date.lastDayOfQuarter()

            //then
            assertThat(lastDayOfQuarter).isEqualTo(date)
        }

        @DisplayName("13주 단위로 구분된 분기 종료일이 반환된다")
        @Test
        fun `13주 단위로 구분된 분기 종료일이 반환된다`() {
            //given
            // 21년도의 첫 월요일은 21년 1월 4일
            val firstQuarterStart = LocalDate.of(2021, 1, 4)
            val secondQuarterStart = firstQuarterStart.plusWeeks(13)
            val thirdQuarterStart = secondQuarterStart.plusWeeks(13)
            val fourthQuarterStart = thirdQuarterStart.plusWeeks(13)

            val firstQuarterEnd = secondQuarterStart.minusDays(1)
            val secondQuarterEnd = thirdQuarterStart.minusDays(1)
            val thirdQuarterEnd = fourthQuarterStart.minusDays(1)
            val fourthQuarterEnd = fourthQuarterStart.plusWeeks(13).minusDays(1)

            //when
            val firstQuarterEnd1 = firstQuarterStart.lastDayOfQuarter()
            val secondQuarterEnd1 = secondQuarterStart.lastDayOfQuarter()
            val thirdQuarterEnd1 = thirdQuarterStart.lastDayOfQuarter()
            val fourthQuarterEnd1 = fourthQuarterStart.lastDayOfQuarter()

            val firstQuarterEnd2 = firstQuarterEnd.lastDayOfQuarter()
            val secondQuarterEnd2 = secondQuarterEnd.lastDayOfQuarter()
            val thirdQuarterEnd2 = thirdQuarterEnd.lastDayOfQuarter()
            val fourthQuarterEnd2 = fourthQuarterEnd.lastDayOfQuarter()

            //then
            assertThat(firstQuarterEnd1).isEqualTo(firstQuarterEnd2).isEqualTo(firstQuarterEnd)
            assertThat(secondQuarterEnd1).isEqualTo(secondQuarterEnd2).isEqualTo(secondQuarterEnd)
            assertThat(thirdQuarterEnd1).isEqualTo(thirdQuarterEnd2).isEqualTo(thirdQuarterEnd)
            assertThat(fourthQuarterEnd1).isEqualTo(fourthQuarterEnd2).isEqualTo(fourthQuarterEnd)

            assertThat(firstQuarterEnd1).isBefore(secondQuarterEnd1)
            assertThat(secondQuarterEnd1).isBefore(thirdQuarterEnd1)
            assertThat(thirdQuarterEnd1).isBefore(fourthQuarterEnd1)
        }

    }


    @DisplayName("test")
    @Test
    fun `test`() {
        //given
        val date1 = LocalDate.of(2023, 1, 2)
        val date2 = LocalDate.of(2023, 4, 2)
        val date3 = LocalDate.of(2023, 4, 3)

        //when
        val firstDayOfQuarter1 = date1.firstDayOfQuarter()
        val firstDayOfQuarter2 = date2.firstDayOfQuarter()
        val firstDayOfQuarter3 = date3.firstDayOfQuarter()

        //then
        println(firstDayOfQuarter1)
        println(firstDayOfQuarter2)
        println(firstDayOfQuarter3)
        println(date3.plusWeeks(13).firstDayOfQuarter())
        println(date3.plusWeeks(26).firstDayOfQuarter())
    }


}