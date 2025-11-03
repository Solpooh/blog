package com.solpooh.boardback.common;

import org.springframework.http.HttpStatus;
public interface ResponseApiInterface {
    String getCode();
    String getMessage();
    HttpStatus getHttpStatus();
}
