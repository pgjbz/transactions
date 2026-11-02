package dev.pgjbz.transactions.web.controllers.handler;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import java.util.HashSet;
import java.util.Set;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import dev.pgjbz.transactions.domain.exceptions.BusinessException;
import dev.pgjbz.transactions.domain.exceptions.DuplicateAccountException;
import dev.pgjbz.transactions.domain.exceptions.NotFoundException;
import dev.pgjbz.transactions.web.dto.response.ErrorDetail;
import dev.pgjbz.transactions.web.dto.response.ErrorResponseDTO;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    @ResponseStatus(BAD_REQUEST)
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            final MethodArgumentNotValidException ex,
            final HttpHeaders headers,
            final HttpStatusCode status,
            final WebRequest request) {

        final Set<ErrorDetail> errors = new HashSet<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            final String fieldName = ((FieldError) error).getField();
            final String errorMessage = error.getDefaultMessage();
            errors.add(new ErrorDetail(fieldName, errorMessage));
        });

        final String path = getPath(request);
        final ErrorResponseDTO errorResponse = new ErrorResponseDTO("invalid request", path,
                status.value(), errors);

        return ResponseEntity.status(status).headers(headers).body(errorResponse);
    }

    @ExceptionHandler(NullPointerException.class)
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    public ResponseEntity<ErrorResponseDTO> handleNullPointerException(final NullPointerException ex,
            final WebRequest request) {
        log.error("null pointer occurred: {}", ex.getMessage(), ex);
        final HttpStatus internalServerError = INTERNAL_SERVER_ERROR;
        final String path = getPath(request);
        return ResponseEntity.status(internalServerError).body(
                new ErrorResponseDTO(internalServerError.getReasonPhrase(), path, internalServerError.value(), null));
    }

    @ResponseStatus(CONFLICT)
    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<ErrorResponseDTO> handleDuplicateKeyException(final DuplicateKeyException ex,
            final WebRequest request) {
        final HttpStatus conflict = CONFLICT;
        final String path = getPath(request);
        return ResponseEntity.status(conflict)
                .body(new ErrorResponseDTO(conflict.getReasonPhrase(), path, conflict.value(), null));
    }

    @ResponseStatus(CONFLICT)
    @ExceptionHandler(DuplicateAccountException.class)
    public ResponseEntity<ErrorResponseDTO> conflictHandler(final BusinessException exception,
            final WebRequest request) {
        final HttpStatus conflict = CONFLICT;
        final String path = getPath(request);
        return ResponseEntity.status(conflict)
                .body(new ErrorResponseDTO(exception.getMessage(), path, conflict.value(), null));
    }

    @ResponseStatus(NOT_FOUND)
    @ExceptionHandler({ NotFoundException.class })
    public ResponseEntity<ErrorResponseDTO> notFound(final BusinessException exception,
            final WebRequest request) {
        final HttpStatus notFound = NOT_FOUND;
        final String path = getPath(request);
        return ResponseEntity.status(notFound)
                .body(new ErrorResponseDTO(exception.getMessage(), path, notFound.value(), null));
    }

    private static String getPath(final WebRequest request) {
        return ((ServletWebRequest) request).getRequest().getRequestURI();
    }
}
