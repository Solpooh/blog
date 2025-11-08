package com.solpooh.boardback.exception;

import com.solpooh.boardback.common.ResponseApi;
import com.solpooh.boardback.dto.common.ResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ResponseDto> customExceptionHandler(CustomException ex) {
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

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ResponseDto.of(ResponseApi.VALIDATION_FAILED, errorMessageList));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseDto> generalExceptionHandler(Exception ex) {
        ex.printStackTrace();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseDto.of(ResponseApi.DATABASE_ERROR));
    }
}
