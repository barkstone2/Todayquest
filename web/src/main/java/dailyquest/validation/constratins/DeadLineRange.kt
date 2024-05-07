package dailyquest.validation.constratins

import dailyquest.validation.validator.DeadLineRangeValidator
import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.annotation.AnnotationTarget.FIELD
import kotlin.reflect.KClass

@Target(FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [DeadLineRangeValidator::class])
annotation class DeadLineRange(
    val message: String,
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)