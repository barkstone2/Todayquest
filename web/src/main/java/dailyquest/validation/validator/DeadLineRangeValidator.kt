package dailyquest.validation.validator

import dailyquest.validation.constratins.DeadLineRange
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import java.time.LocalDateTime

class DeadLineRangeValidator : ConstraintValidator<DeadLineRange, LocalDateTime> {
    override fun isValid(value: LocalDateTime?, context: ConstraintValidatorContext?): Boolean {
        if (value != null) {
            val now = LocalDateTime.now().withSecond(0).withNano(0)
            var nextReset = now.withHour(6).withMinute(0)
            if(now.isEqual(nextReset) || now.isAfter(nextReset)) nextReset = nextReset.plusDays(1L)
            return value.isAfter(now.plusMinutes(5)) && value.isBefore(nextReset.minusMinutes(5))
        }
        return true
    }
}