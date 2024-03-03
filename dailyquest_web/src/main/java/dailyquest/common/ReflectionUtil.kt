package dailyquest.common

import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotations
import kotlin.reflect.full.memberProperties

class ReflectionUtil {
    companion object {
        @JvmStatic
        fun <T : Any> getAnnotatedProperties(targetClass: KClass<T>, targetAnnotation: KClass<out Annotation>): List<KProperty1<T, *>> {
            val annotatedProperties = targetClass.memberProperties.filter { it.findAnnotations(targetAnnotation).isNotEmpty() }
            return annotatedProperties
        }
    }
}