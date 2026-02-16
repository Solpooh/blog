package com.solpooh.boardback.exception;

import com.solpooh.boardback.common.ResponseApi;
import com.solpooh.boardback.dto.common.ResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ResponseDto> customExceptionHandler(CustomException ex) {
        log.debug("CustomException 발생 - code: {}, message: {}", ex.getCode(), ex.getMessage());
        return ResponseEntity.status(ex.getStatus())
                .body(new ResponseDto(ex.getCode(), ex.getMessage(), null));
    }

    // 검증 실패 시 예외 발생 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseDto> validationExceptionHandler(MethodArgumentNotValidException ex) {
        List<String> errorMessageList = ex.getFieldErrors()
                .stream()
                .map(objectError -> {
                    String format = "%s: { %s } 은 %s";
                    return String.format(format, objectError.getField(), objectError.getRejectedValue(), objectError.getDefaultMessage());
                })
                .toList();

        log.warn("Validation 실패 - errors: {}", errorMessageList);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ResponseDto.of(ResponseApi.VALIDATION_FAILED, errorMessageList));
    }

    // 낙관적/비관적 락 충돌 (동시성 제어)
    @ExceptionHandler({OptimisticLockingFailureException.class, PessimisticLockingFailureException.class})
    public ResponseEntity<ResponseDto> lockingExceptionHandler(Exception ex) {
        log.warn("Lock 충돌 발생 - type: {}, message: {}",
                ex.getClass().getSimpleName(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ResponseDto.of(ResponseApi.TRANSCRIPT_PROCESSING));
    }

    // 데이터 무결성 위반 (중복 키 등)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ResponseDto> dataIntegrityExceptionHandler(DataIntegrityViolationException ex) {
        log.warn("데이터 무결성 위반 - message: {}", ex.getMostSpecificCause().getMessage());
        // 대부분 동시 INSERT로 인한 중복 키 충돌이므로 CONFLICT 반환
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ResponseDto.of(ResponseApi.TRANSCRIPT_PROCESSING));
    }

    // 기타 데이터 접근 예외
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ResponseDto> dataAccessExceptionHandler(DataAccessException ex) {
        log.error("DataAccessException 발생 - type: {}, message: {}",
                ex.getClass().getSimpleName(), ex.getMostSpecificCause().getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseDto.of(ResponseApi.DATABASE_ERROR));
    }

    // 클라이언트가 응답 수신 전 연결을 끊은 경우 (Broken Pipe)
    // 응답을 쓸 수 없으므로 아무것도 반환하지 않고 즉시 스레드를 해제한다
    @ExceptionHandler(AsyncRequestNotUsableException.class)
    public void brokenPipeHandler(AsyncRequestNotUsableException ex) {
        log.debug("Client disconnected (Broken Pipe) - {}", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseDto> generalExceptionHandler(Exception ex) {
        log.error("처리되지 않은 예외 발생 - type: {}, message: {}",
                ex.getClass().getSimpleName(), ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseDto.of(ResponseApi.DATABASE_ERROR));
    }
}
