package dailyquest.exception

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler
import java.lang.reflect.Method

class ElasticAsyncUncaughtExceptionHandler : AsyncUncaughtExceptionHandler {
    private val logger: Logger = LoggerFactory.getLogger("asyncErrorLogger")
    override fun handleUncaughtException(ex: Throwable, method: Method, vararg params: Any?) {
        logger.error(
            "[${method.declaringClass.simpleName}#${method.name}(${method.parameterTypes.joinToString(",") { it.simpleName }})]" +
                    " --- with params ${params.contentToString()}"
        )
    }
}