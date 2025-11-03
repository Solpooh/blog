package com.solpooh.boardback.exception;

import com.solpooh.boardback.common.ResponseApi;
import com.solpooh.boardback.dto.response.ResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(CustomException.class)
    public ResponseDto<Void> customExceptionHandler(CustomException ex) {
        return new ResponseDto<>(
                ex.getCode(),
                ex.getMessage(),
                null
        );
    }
    // 검증 실패 시 예외 발생 처리
//    @ExceptionHandler( {MethodArgumentNotValidException.class, HttpMessageNotReadableException.class} )
//    public ResponseDto> validationExceptionHandler() {
//        return ResponseDto.validationFailed();
//    }
//

    @ExceptionHandler(Exception.class)
    public ResponseDto<Void> generalExceptionHandler(Exception ex) {
        ex.printStackTrace();

        return ResponseDto.of(ResponseApi.DATABASE_ERROR);
    }
}
