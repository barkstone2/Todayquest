package dailyquest.quest.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("퀘스트 검색 조건 DTO 단위 테스트")
public class QuestSearchConditionUnitTest {

    @DisplayName("생성자 호출 시 page 번호가 null이면 0으로 처리된다")
    @Test
    public void testDefaultValueFolPage() throws Exception {
        //given
        Integer page = null;

        //when
        QuestSearchCondition condition = new QuestSearchCondition(page, null, null, null, null, null);

        //then
        assertThat(condition.page()).isNotNull();
        assertThat(condition.page()).isEqualTo(0);
    }


    @DisplayName("isKeywordSearch 메서드 호출 시")
    @Nested
    class TestForIsKeywordSearchMethod {
        @DisplayName("키워드 타입과 값이 존재하면 true 를 반환한다")
        @Test
        public void returnTrueWhenKeywordTypeAndKeywordExists() throws Exception {
            //given
            QuestSearchKeywordType keywordType = QuestSearchKeywordType.ALL;
            String keyword = "keyword";
            QuestSearchCondition condition = new QuestSearchCondition(null, null, keywordType, keyword, null, null);

            //when
            boolean isKeywordSearch = condition.isKeywordSearch();

            //then
            assertThat(isKeywordSearch).isTrue();
        }

        @DisplayName("키워드 타입과 값 중 하나가 null이면 false 를 반환한다")
        @Test
        public void returnFalseWhenOneOfElementIsNull() throws Exception {
            //given
            QuestSearchKeywordType keywordType = QuestSearchKeywordType.ALL;
            String keyword = "keyword";
            QuestSearchCondition keywordNullCondition = new QuestSearchCondition(null, null, keywordType, null, null, null);
            QuestSearchCondition keywordTypeNullCondition = new QuestSearchCondition(null, null, null, keyword, null, null);

            //when
            boolean keywordNullConditionResult = keywordNullCondition.isKeywordSearch();
            boolean keywordTypeNullConditionResult = keywordTypeNullCondition.isKeywordSearch();

            //then
            assertThat(keywordNullConditionResult).isFalse();
            assertThat(keywordTypeNullConditionResult).isFalse();
        }
    }

    @DisplayName("getStartResetTime 호출 시")
    @Nested
    class TestGetStartResetTime {
        @DisplayName("startDate가 null이면 null을 반환한다")
        @Test
        public void ifStartDateIsNullThanReturnNull() throws Exception {
            //given
            QuestSearchCondition condition = new QuestSearchCondition(null, null, null, null, null, null);

            //when
            LocalDateTime startResetTime = condition.getStartResetTime();

            //then
            assertThat(startResetTime).isNull();
        }

        @DisplayName("startDate가 null이 아니면 startDate 오전 6시의 LocalDateTime을 반환한다")
        @Test
        public void ifStartDateIsNotNullThanReturnStartDateAt6Am() throws Exception {
            //given
            LocalDate startDate = LocalDate.now();
            QuestSearchCondition condition = new QuestSearchCondition(null, null, null, null, startDate, null);

            //when
            LocalDateTime startResetTime = condition.getStartResetTime();

            //then
            assertThat(startResetTime).isEqualTo(LocalDateTime.of(startDate, LocalTime.of(6, 0)));
        }

    }

    @DisplayName("getEndResetTime 호출 시")
    @Nested
    class TestGetEndResetTime {
        @DisplayName("endDate가 null이면 null을 반환한다")
        @Test
        public void ifEndDateIsNullThanReturnNull() throws Exception {
            //given
            QuestSearchCondition condition = new QuestSearchCondition(null, null, null, null, null, null);

            //when
            LocalDateTime endResetTime = condition.getEndResetTime();

            //then
            assertThat(endResetTime).isNull();
        }

        @DisplayName("endDate가 null이 아니면 endDate 다음 날 오전 6시의 LocalDateTime을 반환한다")
        @Test
        public void ifEndDateIsNotNullThanReturnNextDayOfEndDateAt6Am() throws Exception {
            //given
            LocalDate endDate = LocalDate.now();
            QuestSearchCondition condition = new QuestSearchCondition(null, null, null, null, null, endDate);

            //when
            LocalDateTime endResetTime = condition.getEndResetTime();

            //then
            assertThat(endResetTime).isEqualTo(LocalDateTime.of(endDate.plusDays(1), LocalTime.of(6, 0)));
        }
    }




}
