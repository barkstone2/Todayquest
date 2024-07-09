package dailyquest.exception

import dailyquest.common.ResponseData
import jakarta.persistence.EntityNotFoundException
import jakarta.persistence.OptimisticLockException
import jakarta.persistence.PersistenceException
import jakarta.validation.ConstraintViolationException
import org.hibernate.StaleObjectStateException
import org.hibernate.dialect.lock.OptimisticEntityLockException
import org.slf4j.LoggerFactory
import org.springframework.context.support.MessageSourceAccessor
import org.springframework.http.HttpStatus
import org.springframework.http.converter.HttpMessageConversionException
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.access.AccessDeniedException
import org.springframework.validation.BindException
import org.springframework.validation.BindingResult
import org.springframework.validation.FieldError
import org.springframework.web.HttpMediaTypeNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.servlet.NoHandlerFoundException
import java.util.function.Consumer

@RestControllerAdvice
class RestApiExceptionHandler(
    private val messageSourceAccessor: MessageSourceAccessor
) {
    var log = LoggerFactory.getLogger(this.javaClass)

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(
        NoHandlerFoundException::class
    )
    fun handlerNotFound(e: NoHandlerFoundException): ResponseData<Void> {
        log.error("[exceptionHandle] ex", e)
        val errorResponse = ErrorResponse(messageSourceAccessor.getMessage("exception.notFound"), HttpStatus.NOT_FOUND)
        return ResponseData(errorResponse)
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(
        EntityNotFoundException::class
    )
    fun entityNotFound(e: EntityNotFoundException): ResponseData<Void> {
        log.error("[exceptionHandle] ex", e)
        val errorResponse = ErrorResponse(e.message, HttpStatus.NOT_FOUND)
        return ResponseData(errorResponse)
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(
        AccessDeniedException::class
    )
    fun accessDenied(e: AccessDeniedException): ResponseData<Void> {
        log.error("[exceptionHandle] ex", e)
        val errorResponse = ErrorResponse(e.message, HttpStatus.FORBIDDEN)
        return ResponseData(errorResponse)
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(
        IllegalStateException::class,
    )
    fun illegalState(e: IllegalStateException): ResponseData<Void> {
        log.error("[exceptionHandle] ex", e)
        val errorResponse = ErrorResponse(e.message, HttpStatus.BAD_REQUEST)
        return ResponseData(errorResponse)
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(
        IllegalArgumentException::class,
    )
    fun illegalExHandle(e: IllegalArgumentException): ResponseData<Void> {
        log.error("[exceptionHandle] ex", e)
        val errorResponse = ErrorResponse(e.message, HttpStatus.BAD_REQUEST)
        return ResponseData(errorResponse)
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(
        HttpMessageNotReadableException::class,
        HttpMessageConversionException::class,
        BindException::class,
        MethodArgumentTypeMismatchException::class,
        HttpMediaTypeNotSupportedException::class,
        ConstraintViolationException::class,
    )
    fun badRequest(e: Exception): ResponseData<Void> {
        log.error("[exceptionHandle]", e)
        val errorResponse = ErrorResponse(messageSourceAccessor.getMessage("exception.badRequest"), HttpStatus.BAD_REQUEST)
        return ResponseData(errorResponse)
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(
        MethodArgumentNotValidException::class,
    )
    fun bindingResultError(e: MethodArgumentNotValidException): ResponseData<Void> {
        log.error("[exceptionHandle]", e)
        return handleBindingResult(e.bindingResult)
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(
        DuplicateNicknameException::class
    )
    fun duplicateNickname(e: DuplicateNicknameException): ResponseData<Void> {
        val errorResponse = ErrorResponse(e.message, HttpStatus.CONFLICT)
        return ResponseData(errorResponse)
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(
        RedisDataNotFoundException::class
    )
    fun serverError(e: RedisDataNotFoundException): ResponseData<Void> {
        log.error("[exceptionHandle] ex", e)
        val errorResponse = ErrorResponse(e.message, HttpStatus.INTERNAL_SERVER_ERROR)
        return ResponseData(errorResponse)
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(OptimisticLockException::class, OptimisticEntityLockException::class, StaleObjectStateException::class)
    fun optimisticLock(e: PersistenceException): ResponseData<Void> {
        val errorResponse = ErrorResponse(messageSourceAccessor.getMessage("exception.optimisticLock"), HttpStatus.CONFLICT)
        return ResponseData(errorResponse)
    }

    private fun <T> handleBindingResult(bindingResult: BindingResult): ResponseData<T> {
        val fieldErrors = bindingResult.fieldErrors
        val errorResponse = ErrorResponse(messageSourceAccessor.getMessage("exception.badRequest"), HttpStatus.BAD_REQUEST)
        fieldErrors.forEach(Consumer { fieldError: FieldError ->
            errorResponse.errors.add(fieldError.field, fieldError.defaultMessage)
        })
        return ResponseData(errorResponse)
    }

}