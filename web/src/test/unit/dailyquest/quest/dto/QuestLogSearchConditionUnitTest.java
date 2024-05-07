package dailyquest.quest.dto;

import dailyquest.common.DateTimeExtensionKt;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.TemporalAdjusters;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("퀘스트 로그 검색 조건 테스트")
public class QuestLogSearchConditionUnitTest {

    @DisplayName("시작 일자 요청 시 조회 타입에 따라 알맞은 값이 반환된다")
    @Test
    public void testGetStartDate() throws Exception {
        //given
        LocalDate now = LocalDate.now();
        QuestLogSearchCondition dailyCondition = new QuestLogSearchCondition(QuestLogSearchType.DAILY, now);
        QuestLogSearchCondition weeklyCondition = new QuestLogSearchCondition(QuestLogSearchType.WEEKLY, now);
        QuestLogSearchCondition monthlyCondition = new QuestLogSearchCondition(QuestLogSearchType.MONTHLY, now);

        //when
        LocalDate dailyStart = dailyCondition.getStartDateOfSearchRange();
        LocalDate weeklyStart = weeklyCondition.getStartDateOfSearchRange();
        LocalDate monthlyStart = monthlyCondition.getStartDateOfSearchRange();

        //then
        assertThat(dailyStart).isEqualTo(now.with(TemporalAdjusters.firstDayOfMonth()));
        assertThat(weeklyStart).isEqualTo(DateTimeExtensionKt.firstDayOfQuarter(now));
        assertThat(monthlyStart).isEqualTo(now.with(TemporalAdjusters.firstDayOfYear()));
    }

    @DisplayName("종료 일자 요청 시 조회 타입에 따라 알맞은 값이 반환된다")
    @Test
    public void testGetEndDate() throws Exception {
        //given
        LocalDate now = LocalDate.now();
        QuestLogSearchCondition dailyCondition = new QuestLogSearchCondition(QuestLogSearchType.DAILY, now);
        QuestLogSearchCondition weeklyCondition = new QuestLogSearchCondition(QuestLogSearchType.WEEKLY, now);
        QuestLogSearchCondition monthlyCondition = new QuestLogSearchCondition(QuestLogSearchType.MONTHLY, now);

        //when
        LocalDate dailyEnd = dailyCondition.getEndDateOfSearchRange();
        LocalDate weeklyEnd = weeklyCondition.getEndDateOfSearchRange();
        LocalDate monthlyEnd = monthlyCondition.getEndDateOfSearchRange();

        //then
        assertThat(dailyEnd).isEqualTo(now.with(TemporalAdjusters.lastDayOfMonth()));
        assertThat(weeklyEnd).isEqualTo(DateTimeExtensionKt.lastDayOfQuarter(now));
        assertThat(monthlyEnd).isEqualTo(now.with(TemporalAdjusters.lastDayOfYear()));
    }

    @DisplayName("응답 맵 생성 요청 시 조회 타입에 따라 알맞은 맵이 반환된다")
    @Test
    public void testCreateResponseMap() throws Exception {
        //given
        LocalDate now = LocalDate.now();
        QuestLogSearchCondition dailyCondition = new QuestLogSearchCondition(QuestLogSearchType.DAILY, now);
        QuestLogSearchCondition weeklyCondition = new QuestLogSearchCondition(QuestLogSearchType.WEEKLY, now);
        QuestLogSearchCondition monthlyCondition = new QuestLogSearchCondition(QuestLogSearchType.MONTHLY, now);

        LocalDate dailyStart = dailyCondition.getStartDateOfSearchRange();
        LocalDate dailyEnd = dailyCondition.getEndDateOfSearchRange().plusDays(1);

        LocalDate weeklyStart = weeklyCondition.getStartDateOfSearchRange();
        LocalDate weeklyEnd = weeklyCondition.getEndDateOfSearchRange().plusDays(1);

        LocalDate monthlyStart = monthlyCondition.getStartDateOfSearchRange();
        LocalDate monthlyEnd = monthlyCondition.getEndDateOfSearchRange().plusDays(1);

        //when
        Map<LocalDate, QuestStatisticsResponse> dailyMap = dailyCondition.createResponseMapOfPeriodUnit();
        Map<LocalDate, QuestStatisticsResponse> weeklyMap = weeklyCondition.createResponseMapOfPeriodUnit();
        Map<LocalDate, QuestStatisticsResponse> monthlyMap = monthlyCondition.createResponseMapOfPeriodUnit();

        //then
        assertThat(dailyMap).containsOnlyKeys(dailyStart.datesUntil(dailyEnd, Period.ofDays(1)).toList());
        assertThat(weeklyMap).containsOnlyKeys(weeklyStart.datesUntil(weeklyEnd, Period.ofWeeks(1)).toList());
        assertThat(monthlyMap).containsOnlyKeys(monthlyStart.datesUntil(monthlyEnd, Period.ofMonths(1)).toList());
    }
}
