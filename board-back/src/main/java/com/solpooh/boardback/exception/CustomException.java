package com.solpooh.boardback.exception;

import com.solpooh.boardback.common.ResponseApiInterface;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CustomException extends RuntimeException {
    private final String code;
    private final HttpStatus status;
    public CustomException(ResponseApiInterface response) {
        super(response.getMessage());

        this.code = response.getCode();
        this.status = response.getHttpStatus();
    }
}
