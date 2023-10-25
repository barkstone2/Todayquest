package dailyquest.quest.dto;

import java.util.List;

public enum QuestSearchKeywordType {
    ALL(List.of(FieldType.TITLE, FieldType.DESCRIPTION, FieldType.DETAIL_TITLES)),
    TITLE(List.of(FieldType.TITLE)),
    TITLE_AND_DESCRIPTION(List.of(FieldType.TITLE, FieldType.DESCRIPTION)),
    DESCRIPTION(List.of(FieldType.DESCRIPTION)),
    DETAIL_TITLES(List.of(FieldType.DETAIL_TITLES));

    public final List<String> fieldNames;

    public static class FieldType {
        public static final String TITLE = "title^2";
        public static final String DESCRIPTION = "description";
        public static final String DETAIL_TITLES = "detailTitles";
    }

    QuestSearchKeywordType(List<String> fieldNames) {
        this.fieldNames = fieldNames;
    }

}