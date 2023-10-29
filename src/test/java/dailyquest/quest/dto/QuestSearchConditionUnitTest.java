package dailyquest.quest.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

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

}
