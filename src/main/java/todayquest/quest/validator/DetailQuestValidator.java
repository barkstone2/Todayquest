package todayquest.quest.validator;

import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.Validator;
import todayquest.quest.dto.DetailQuestRequestDto;
import todayquest.quest.dto.QuestRequestDto;
import todayquest.quest.entity.DetailQuestType;

import java.util.List;

@Component
public class DetailQuestValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return QuestRequestDto.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        if(!supports(target.getClass())) return;

        QuestRequestDto dto = (QuestRequestDto) target;
        BindingResult bindingResult = (BindingResult) errors;

        List<DetailQuestRequestDto> detailQuests = dto.getDetailQuests();
        for (int i = 0; i < detailQuests.size(); i++) {
            DetailQuestRequestDto detailQuest = detailQuests.get(i);
            String title = detailQuest.getTitle();

            if(title.isEmpty()) {
                bindingResult.addError(new FieldError("quest", "detailQuests["+i+"].title", title, false, new String[]{"NotBlank.detail-quest.title"}, new Object[]{"세부 퀘스트 명"}, ""));
                bindingResult.rejectValue("detailQuests["+i+"]", "NotBlank.detail-quest.title", new Object[]{"세부 퀘스트 명"},"");
            }

            if(title.length() > 50) {
                bindingResult.addError(new FieldError("quest", "detailQuests["+i+"].title", title, false, new String[]{"Size.detail-quest.title"}, new Object[]{"세부 퀘스트 명"}, ""));
                bindingResult.rejectValue("detailQuests["+i+"]", "Size.detail-quest.title", new Object[]{"세부 퀘스트 명"}, "");
            }

            Short targetCount = detailQuest.getTargetCount();
            if(detailQuest.getType().equals(DetailQuestType.COUNT) && targetCount == null) {
                bindingResult.addError(new FieldError("quest", "detailQuests["+i+"].targetCount", null, false, new String[]{"NotBlank.detail-quest.targetCount"}, new Object[]{"목표 카운트"}, ""));
                bindingResult.rejectValue("detailQuests["+i+"]", "NotBlank.detail-quest.targetCount", new Object[]{"목표 카운트"}, "");
            }

            if (detailQuest.getType().equals(DetailQuestType.COUNT) && targetCount != null && (targetCount > 255 || targetCount < 1)) {
                bindingResult.addError(new FieldError("quest", "detailQuests["+i+"].targetCount", targetCount, false, new String[]{"Range.detail-quest.targetCount"}, null, ""));
                bindingResult.rejectValue("detailQuests["+i+"]", "Range.detail-quest.targetCount", "");
            }

        }

    }
}
