package dailyquest.common

import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotations
import kotlin.reflect.full.memberProperties

class ReflectionUtil {
    companion object {
        @JvmStatic
        fun <T : Any> getAnnotatedPropertiesMap(instance: T, targetAnnotation: KClass<out Annotation>): Map<String, Any> {
            val result = mutableMapOf<String, Any>()
            val annotatedProperties = this.getAnnotatedProperties(instance.javaClass.kotlin, targetAnnotation)
            annotatedProperties.forEach { result[it.name] = it.get(instance).toString() }
            return result
        }

        private fun <T : Any> getAnnotatedProperties(targetClass: KClass<T>, targetAnnotation: KClass<out Annotation>): List<KProperty1<T, *>> {
            val annotatedProperties = targetClass.memberProperties.filter { it.findAnnotations(targetAnnotation).isNotEmpty() }
            return annotatedProperties
        }
    }
}