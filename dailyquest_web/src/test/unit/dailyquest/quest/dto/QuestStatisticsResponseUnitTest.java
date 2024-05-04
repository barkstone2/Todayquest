package dailyquest.quest.dto;

import dailyquest.quest.entity.QuestState;
import dailyquest.quest.entity.QuestType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("퀘스트 통계 응답 DTO 유닛 테스트")
public class QuestStatisticsResponseUnitTest {

    @DisplayName("addStateCount 호출 시 상태에 따라 카운트가 더해진다")
    @Test
    public void testAddStateCount() throws Exception {
        //given
        QuestStatisticsResponse response = new QuestStatisticsResponse(LocalDate.now());
        int completeCount = 3;
        int failCount = 4;
        int discardCount = 1;

        //when
        response.addStateCount(QuestState.COMPLETE.name(), completeCount);
        response.addStateCount(QuestState.FAIL.name(), failCount);
        response.addStateCount(QuestState.DISCARD.name(), discardCount);

        //then
        assertThat(response.getCompleteCount()).isEqualTo(completeCount);
        assertThat(response.getFailCount()).isEqualTo(failCount);
        assertThat(response.getDiscardCount()).isEqualTo(discardCount);

        assertThat(response.getMainCount()).isEqualTo(0);
        assertThat(response.getSubCount()).isEqualTo(0);
    }

    @DisplayName("addTypeCount 호출 시 타입에 따라 카운트가 더해진다")
    @Test
    public void testAddTypeCount() throws Exception {
        //given
        QuestStatisticsResponse response = new QuestStatisticsResponse(LocalDate.now());
        int mainCount = 3;
        int subCount = 1;

        //when
        response.addTypeCount(QuestType.MAIN.name(), mainCount);
        response.addTypeCount(QuestType.SUB.name(), subCount);

        //then
        assertThat(response.getMainCount()).isEqualTo(mainCount);
        assertThat(response.getSubCount()).isEqualTo(subCount);

        assertThat(response.getCompleteCount()).isEqualTo(0);
        assertThat(response.getFailCount()).isEqualTo(0);
        assertThat(response.getDiscardCount()).isEqualTo(0);
    }

    @DisplayName("calcTypeRatio 호출 시 전체 타입 중 메인의 비율을 반환한다")
    @Test
    public void testCalcTypeRatio() throws Exception {
        //given
        QuestStatisticsResponse response = new QuestStatisticsResponse(LocalDate.now());
        int mainCount = 5;
        int subCount = 5;
        long ratio = Math.round(mainCount * 100d / (mainCount + subCount));
        response.addTypeCount(QuestType.MAIN.name(), mainCount);
        response.addTypeCount(QuestType.SUB.name(), subCount);

        //when
        response.calcTypeRatio();

        //then
        assertThat(response.getTypeRatio()).isEqualTo(ratio);
    }

    @DisplayName("calcStateRatio 호출 시 전체 상태 중 완료 상태 비율을 반환한다")
    @Test
    public void testCalcStateRatio() throws Exception {
        //given
        QuestStatisticsResponse response = new QuestStatisticsResponse(LocalDate.now());
        int completeCount = 5;
        int failCount = 4;
        int discardCount = 1;
        int registeredCount = 10;
        long stateRatio = Math.round(completeCount * 100d / registeredCount);

        response.addStateCount(QuestState.PROCEED.name(), registeredCount);
        response.addStateCount(QuestState.COMPLETE.name(), completeCount);
        response.addStateCount(QuestState.FAIL.name(), failCount);
        response.addStateCount(QuestState.DISCARD.name(), discardCount);

        //when
        response.calcStateRatio();

        //then
        assertThat(response.getStateRatio()).isEqualTo(stateRatio);
    }

    @DisplayName("combineCount 호출 시 두 DTO의 카운트를 합친다")
    @Test
    public void testCombineCount() throws Exception {
        //given
        QuestStatisticsResponse response1 = new QuestStatisticsResponse(LocalDate.now());
        QuestStatisticsResponse response2 = new QuestStatisticsResponse(LocalDate.now());

        int completeCount = 1;
        int failCount = 2;
        int discardCount = 3;
        int mainCount = 4;
        int subCount = 5;

        response1.addStateCount(QuestState.COMPLETE.name(), completeCount);
        response1.addStateCount(QuestState.FAIL.name(), failCount);
        response1.addStateCount(QuestState.DISCARD.name(), discardCount);
        response1.addTypeCount(QuestType.MAIN.name(), mainCount);
        response1.addTypeCount(QuestType.SUB.name(), subCount);

        response2.addStateCount(QuestState.COMPLETE.name(), completeCount * 2);
        response2.addStateCount(QuestState.FAIL.name(), failCount * 2);
        response2.addStateCount(QuestState.DISCARD.name(), discardCount * 2);
        response2.addTypeCount(QuestType.MAIN.name(), mainCount * 2);
        response2.addTypeCount(QuestType.SUB.name(), subCount * 2);

        //when
        response1.combineCount(response2);

        //then
        assertThat(response1.getCompleteCount()).isEqualTo(completeCount * 3);
        assertThat(response1.getFailCount()).isEqualTo(failCount * 3);
        assertThat(response1.getDiscardCount()).isEqualTo(discardCount * 3);

        assertThat(response1.getMainCount()).isEqualTo(mainCount * 3);
        assertThat(response1.getSubCount()).isEqualTo(subCount * 3);
    }

}
