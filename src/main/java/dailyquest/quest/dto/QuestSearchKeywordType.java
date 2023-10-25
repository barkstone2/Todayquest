package dailyquest.quest.dto;

public enum QuestSearchKeywordType {
    ALL(new String[]{FieldType.TITLE, FieldType.DESCRIPTION, FieldType.DETAIL_TITLES}),
    TITLE(new String[]{FieldType.TITLE}),
    TITLE_AND_DESCRIPTION(new String[]{FieldType.TITLE, FieldType.DESCRIPTION}),
    DESCRIPTION(new String[]{FieldType.DESCRIPTION}),
    DETAIL_TITLES(new String[]{FieldType.DETAIL_TITLES});

    public final String[] fieldNames;

    public static class FieldType {
        public static final String TITLE = "title";
        public static final String DESCRIPTION = "description";
        public static final String DETAIL_TITLES = "detailTitles";
    }

    QuestSearchKeywordType(String[] fieldNames) {
        this.fieldNames = fieldNames;
    }

}